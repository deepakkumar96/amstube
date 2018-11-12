package com.assignment.amstube.indexing;

public class IndexingResult {
    private Result result;
    public static IndexingResult UNSUCCESFUL = new IndexingResult(Result.UNSUCESSFUL);
    public static IndexingResult SUCESSFUL = new IndexingResult(Result.SUCESSFUL);
    private String uploadedPath;

    public IndexingResult(Result result){
        this.result = result;
    }


    public void setResultType(Result res){
        this.result = res;
    }

    public static IndexingResult successResult(){
        return new IndexingResult(Result.SUCESSFUL);
    }




    @Override
    public String toString(){
        return result.toString();
    }

    public String getUploadedPath() {
        return uploadedPath;
    }

    public void setUploadedPath(String uploadedPath) {
        this.uploadedPath = uploadedPath;
    }

    enum Result{
        SUCESSFUL, UNSUCESSFUL
    }
}
