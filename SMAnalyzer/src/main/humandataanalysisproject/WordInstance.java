package main.humandataanalysisproject;

public class WordInstance implements Comparable<WordInstance> {
    
    private final String WORD;
    private int Count;
    
    public WordInstance(String s) {
        WORD = s;
        Count = 1;
    }
    
    public void increment() {
        Count++;
    }
    
    public String getWord() {
        return WORD;
    }
    
    public int getCount() {
        return Count;
    }
    
    @Override
    public String toString() {
        return "Keyword: " + WORD + " Count: " + Count + "\n";
    }
    
    @Override
    public int compareTo(WordInstance otherInstance) throws UnsupportedOperationException {
        return Integer.compare(this.Count, otherInstance.Count);
    }

}