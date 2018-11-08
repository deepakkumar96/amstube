package com.assignment.amstube.indexing;

public class IndexingResult {
    private Result result;
    public static IndexingResult UNSUCCESFUL = new IndexingResult(Result.UNSUCESSFUL);
    public static IndexingResult SUCESSFUL = new IndexingResult(Result.SUCESSFUL);

    public IndexingResult(Result result){
        this.result = result;
    }


    public void setResultType(Result res){
        this.result = res;
    }

    @Override
    public String toString(){
        return result.toString();
    }

    enum Result{
        SUCESSFUL, UNSUCESSFUL
    }
}
