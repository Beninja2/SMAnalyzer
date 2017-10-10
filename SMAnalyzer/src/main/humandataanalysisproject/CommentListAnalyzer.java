package main.humandataanalysisproject;

import com.restfb.util.StringUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CommentListAnalyzer {

    //Declare Class Variables
    private final String DictionaryPath = "externalfiles/BigAssDictionaryFromPrinceton.txt";
    private final String BlackListPath = "externalfiles/BlackList.txt";
    private ArrayList<CommentInstance> AllComments;
    private final DictionaryInstance Dictionary;
    private ArrayList<WordInstance> AllUniqueWords;
    private ArrayList<WordInstance> AllUniqueWordsFiltered;
    private ArrayList<WordInstance> BlackList;
    private ArrayList<CommentGroup> Groups;
    private final int NUMBER_OF_GROUPS = 10;

    public CommentListAnalyzer() throws IOException {
        //Initialize Class Variables
        Dictionary = new DictionaryInstance(DictionaryPath, "English");
        AllComments = new ArrayList();
        AllUniqueWords = new ArrayList();
        AllUniqueWordsFiltered = new ArrayList();
        BlackList = new ArrayList();
        Groups = new ArrayList();

        //Import words into BlackList
        BufferedReader br;
        br = new BufferedReader(new FileReader(BlackListPath));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            BlackList.add(new WordInstance(sCurrentLine));
        }
    }

    public void setComments(ArrayList<String> post) throws IOException {
        //Adding ArrayList of strings from input to ArrayList of CommentInstances
        for (int x = 0; x < post.size(); x++) {
            CommentInstance currentInstance = new CommentInstance(post.get(x), Dictionary.getDictionaryInstance());
            if (currentInstance.getIsEnglish()) {
                AllComments.add(currentInstance);
            }
        }

        //Loading the unique words from all CommentInstances into a single ArrayList that can be sorted
        ArrayList<WordInstance> currentList;
        for (int y = 0; y < AllComments.size(); y++) {
            currentList = AllComments.get(y).getUniqueWordList();
            for (int x = 0; x < currentList.size(); x++) {
                AllUniqueWords.add(currentList.get(x));
            }
        }
        Collections.sort(AllUniqueWords);

        //Call Method to filter out the crap 
        AllUniqueWordsFiltered = filterMeaninglessWords(AllUniqueWords);
        System.out.println(AllUniqueWordsFiltered);

        //Loading the sorted list of AllUniqueWordsFiltered into another ArrayList that is formatted to store
        //Only one instance of each word but with the number of unique reoccurances preceeding it
        //Once sorted, the highest frequency words will bubble to the top
        //deprecate this section?
        int currentCountForWord = 1;
        for (int k = 0; k < AllUniqueWordsFiltered.size() - 1; k++) {
            if (AllUniqueWordsFiltered.get(k).equals(AllUniqueWordsFiltered.get(k + 1))) {
                currentCountForWord += 1;
            } else {
                //AllUniqueWordsFilteredWithCounts.add(currentCountForWord + AllUniqueWordsFiltered.get(k));
                currentCountForWord = 1;
            }
        }
    }

    public ArrayList<WordInstance> filterMeaninglessWords(ArrayList<WordInstance> input) {
        for (int x = 0; x < input.size(); x++) {
            for (int y = 0; y < BlackList.size(); y++) {
                if (BlackList.get(y).getWord().equals(input.get(x).getWord())) {
                    input.remove(x);
                    x--;
                }
            }
        }
        return input;
    }

    public void groupComments() {
        int targetIndex = 0;
        String keyword = "";
        //set targetIndex equal to last element in list
        targetIndex = AllUniqueWordsFiltered.size() - 1;
        //create a group for the last x elements in list, where x is 
        //NUMBER_OF_GROUPS
        if (AllUniqueWordsFiltered.size() > NUMBER_OF_GROUPS) {
            for (int k = 0; k < NUMBER_OF_GROUPS; k++) {
                keyword = AllUniqueWordsFiltered.get(targetIndex).getWord();
                Groups.add(new CommentGroup(keyword));
                targetIndex--;
            }
        } else {
            for (int k = 0; k < AllUniqueWordsFiltered.size(); k++) {
                keyword = AllUniqueWordsFiltered.get(targetIndex).getWord();
                Groups.add(new CommentGroup(keyword));
                targetIndex--;
            }
        }
        //iterate through AllComments and add any that contain a grouped keyword
        //to that group. Might be more efficient method
        ArrayList<WordInstance> wordList;
        for (CommentGroup g : Groups) {
            keyword = g.getKeyword();
            for (CommentInstance c : AllComments) {
                wordList = c.getUniqueWordList();
                for (WordInstance w : wordList) {
                    if (w.getWord().equals(keyword)) {
                        g.addComment(c);
                    }
                }
            }
        }
        //output groups to console
        System.out.print("Total Groups: " + Groups.size()
                + "\n----------------------------------\n");
        for (CommentGroup g : Groups) {
            System.out.print(g);
        }
    }

    public void clearArray() {
        AllComments.clear();
        AllUniqueWords.clear();
        AllUniqueWordsFiltered.clear();
        Groups.clear();
    }
}