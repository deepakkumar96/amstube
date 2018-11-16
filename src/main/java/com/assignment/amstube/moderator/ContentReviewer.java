package com.assignment.amstube.moderator;

import com.assignment.amstube.indexing.IndexingLogQueue;

public class ContentReviewer {

    public static boolean voilateContatePolicy(String moderatorContent){
        IndexingLogQueue.INSTANCE.enqueue("Reviewer Started");
        double reviewRecomendedTrue = countOccurance(moderatorContent, "true");
        double reviewRecomendedFalse = countOccurance(moderatorContent, "false");

        System.err.println("Moderator Ratio : "+((double)(reviewRecomendedTrue/reviewRecomendedFalse)*100.0)+
                            "   Count : "+reviewRecomendedFalse + ", " + reviewRecomendedTrue);
        IndexingLogQueue.INSTANCE.enqueue("Moderator Ratio : "+((double)(reviewRecomendedTrue/reviewRecomendedFalse)*100.0)+
                "   Count : "+reviewRecomendedFalse + ", " + reviewRecomendedTrue);

        if(((double)(reviewRecomendedTrue/reviewRecomendedFalse)*100.0) >= 10)
            return true;
        else return false;

    }

    private static int countOccurance(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }


}
