package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;

import java.util.*;
import java.io.PrintWriter;


public class Index5 {

    //--------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    /**
     * Initializes a new Index5 instance, creating empty hashmaps
     * for the sources and the inverted index.
     */
    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    /**
     * Sets the total number of documents in the collection.
     *
     * @param n the number of documents
     */
    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    /**
     * Prints the document IDs of a posting list in a comma-separated format.
     *
     * @param p the head of the posting list to print
     */
    public void printPostingList(Posting p) {
        System.out.print("[");
        while (p != null) {
            System.out.print(p.docId);          // print the doc ID
            if (p.next != null) {               // only print comma if there's another element after this one
                System.out.print(",");
            }
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    /**
     * Prints the entire dictionary and its associated posting lists,
     * including term strings, document frequencies, and the list of document IDs.
     */
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //-----------------------------------------------
    /**
     * Reads a list of local file paths and builds the inverted index by
     * processing each document line by line.
     *
     * @param files an array of file paths to read and index
     */
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    flen += indexOneLine(ln, fid, flen);
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        //   printDictionary();
    }

    //----------------------------------------------------------------------------
    /**
     * Parses a single line of text from a document, ignoring stop words,
     * stemming valid terminology, and updating the dictionary and posting lists.
     *
     * @param ln  the line of text to process
     * @param fid the document ID the line belongs to
     * @return the number of valid tokens found in the line
     */
    public int indexOneLine(String ln, int fid, int pos_offset) {
        int flen = 0;

        String[] words = ln.split("\\W+");
      //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        int word_pos = pos_offset;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                word_pos++;
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).pList.positions.add(word_pos);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last.next.positions.add(word_pos);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
                index.get(word).last.positions.add(word_pos);
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }
            word_pos++;

        }
        return flen;
    }

//----------------------------------------------------------------------------
    /**
     * Checks whether a given word is a stop word or is too short
     * to be indexed.
     *
     * @param word the word to check
     * @return true if the word should be ignored, false otherwise
     */
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------

    /**
     * Reduces a given word to its stem root. (Currently skips stemming).
     *
     * @param word the word to stem
     * @return the stemmed word
     */
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    /**
     * Computes the intersection of two sorted posting lists.
     *
     * @param pL1 the head of the first posting list
     * @param pL2 the head of the second posting list
     * @return a new posting list representing the intersection
     */
    Posting intersect(Posting pL1, Posting pL2) {
///****  -1-   complete after each comment ****
//   INTERSECT ( p1 , p2 )
//          1  answer ←      {}
        Posting answer = null;
        Posting last = null;
//      2 while p1  != NIL and p2  != NIL
        while (pL1 != null && pL2 != null) {

            //          3 do if docID ( p 1 ) = docID ( p2 )
            if (pL1.docId == pL2.docId) {

//          4   then ADD ( answer, docID ( p1 ))
                // answer.add(pL1.docId);
                Posting newNode = new Posting(pL1.docId);

                if (answer == null) {
                    answer = newNode;
                    last = answer;
                } else {
                    last.next = newNode;
                    last = last.next;
                }

//          5       p1 ← next ( p1 )
                pL1 = pL1.next;

//          6       p2 ← next ( p2 )
                pL2 = pL2.next;

            }

 //          7   else if docID ( p1 ) < docID ( p2 )
            else if (pL1.docId < pL2.docId) {

//          8        then p1 ← next ( p1 )
                pL1 = pL1.next;

            } else {
//          9        else p2 ← next ( p2 )
                pL2 = pL2.next;
            }
        }

//      10 return answer
        return answer;
    }

    /**
     * Queries the index for a multi-term phrase by computing intersections
     * iteratively. Returns a formatted string with the matching document results.
     *
     * @param phrase the string containing the search terms
     * @return an aggregated string displaying related documents and metadata
     */
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        if (len == 0) {
            return "No results";
        }

        //fix this if word is not in the hash table will crash...
        // FIXED
        String first = words[0].toLowerCase();

        if (!index.containsKey(first)) {
            return "No results";
        }

        Posting posting = index.get(first).pList;
        int i = 1;
        while (i < len) {
            if (!index.containsKey(words[i].toLowerCase())) {
                return "No results";
            }
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);

            i++;
        }
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }


    //---------------------------------
    /**
     * Sorts an array of strings in alphabetical order using a bubble sort algorithm.
     *
     * @param words the array of strings to sort
     * @return the sorted array
     */
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------

    /**
     * Persists the collection sources metadata and inverted index dictionary
     * out to a target storage file path on the local disk.
     *
     * @param storageName the name of the file
     */
    public void store(String storageName) {
        try {
            String pathToStorage = "/Users/youssef/Downloads/4_5990032386258115504/tmp11/tmp11/rl/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf);
                    for (int pos : p.positions) {
                        wr.write("," + pos);
                    }
                    wr.write(":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================
    /**
     * Verifies if a given index storage file currently exists on disk.
     *
     * @param storageName the name of the file to check
     * @return true if the index cache file exists, false otherwise
     */
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("/Users/youssef/Downloads/4_5990032386258115504/tmp11/tmp11/rl/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }
//----------------------------------------------------
    /**
     * Creates a new placeholder index storage file with an "end" marker.
     *
     * @param storageName the target storage file name
     */
    public void createStore(String storageName) {
        try {
            String pathToStorage = "/Users/youssef/Downloads/4_5990032386258115504/tmp11/tmp11/rl/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------
     //load index from hard disk into memory
    /**
     * Loads the stored inverted index from local disk into memory.
     * Reconstructs the source documents mappings and dictionary entries.
     *
     * @param storageName the target file to load from
     * @return the reconstructed dictionary/inverted index map
     */
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "/Users/youssef/Downloads/4_5990032386258115504/tmp11/tmp11/rl/"+storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    Posting newPosting = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                    for (int j = 2; j < ss1bx.length; j++) {
                        newPosting.positions.add(Integer.parseInt(ss1bx[j]));
                    }
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = newPosting;
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = newPosting;
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }


    public HashMap<Integer, SourceRecord> getSources() {
        return new HashMap<>(sources);
    }

    public int getIndexSize() {
        return index.size();
    }
    /**
     * Finds documents where two posting lists have words
     * appearing exactly 'gap' positions apart.
     */
    Posting positionalIntersect(Posting pL1, Posting pL2, int gap) {
        Posting answer = null;
        Posting last = null;

        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                List<Integer> matchedPositions = new ArrayList<>();
                for (int pos1 : pL1.positions) {
                    for (int pos2 : pL2.positions) {
                        if (pos2 == pos1 + gap) {
                            matchedPositions.add(pos1);
                            break;
                        }
                    }
                }
                if (!matchedPositions.isEmpty()) {
                    Posting newNode = new Posting(pL1.docId);
                    newNode.positions.addAll(matchedPositions);
                    if (answer == null) {
                        answer = newNode;
                        last = answer;
                    } else {
                        last.next = newNode;
                        last = last.next;
                    }
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    /**
     * Searches for documents where query words appear
     * consecutively (phrase search using positional index).
     */
    public String phraseSearch(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");

        // filter stop words
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            word = word.toLowerCase();
            if (!stopWord(word)) {
                filteredWords.add(stemWord(word));
            }
        }

        if (filteredWords.isEmpty()) return "No results";

        String first = filteredWords.get(0);
        if (!index.containsKey(first)) return "No results";

        Posting posting = index.get(first).pList;

        for (int i = 1; i < filteredWords.size(); i++) {
            String w = filteredWords.get(i);
            if (!index.containsKey(w)) return "No results";
            posting = positionalIntersect(posting, index.get(w).pList, i);
            if (posting == null) return "No results";
        }

        while (posting != null) {
            result += "\t" + posting.docId + " - "
                    + sources.get(posting.docId).title + " - "
                    + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result.isEmpty() ? "No results" : result;
    }

    public void buildIndexFromWeb(List<String> pageContents, List<String> pageUrls) {
        int fid = 0;
        for (int i = 0; i < pageContents.size(); i++) {
            String content = pageContents.get(i);
            String url = pageUrls.get(i);

            // store page metadata (id, url as path, url as title, no raw text)
            sources.put(fid, new SourceRecord(fid, url, url, "notext"));

            // split content into lines and index each one
            String[] lines = content.split("\n");
            int flen = 0;
            int posOffset = 0;
            for (String line : lines) {
                int wordsInLine = indexOneLine(line, fid, posOffset);
                flen += wordsInLine;
                posOffset += wordsInLine;
            }

            sources.get(fid).length = flen;
            System.out.println("Indexed page " + fid + ": " + url + " (" + flen + " words)");
            fid++;
        }
        System.out.println("\n--- Indexing complete: " + fid + " pages, "
                + index.size() + " unique terms ---\n");
    }
}
