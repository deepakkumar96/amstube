package com.assignment.amstube.indexing;

import com.assignment.amstube.models.LogQueue;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.authentication.*;
import com.microsoft.windowsazure.services.media.models.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoIndexer implements Indexer {

    private static MediaContract mediaService;

    // Media Services account credentials configuration
    private   String tenant = "2cb270e2-de00-4b6e-9586-41c025816210";
    private   String clientId = "de8387b2-0a4b-4cfc-b24b-6bffb1b50c57";
    private   String clientKey = "2D5PGa55UMMnxHDwMhDXeVA3IWH63IpwmzppoIURNvI=";
    private   String restApiEndpoint = "https://mediatest4.restv2.eastus.media.azure.net/api/";
    // Input file
    private  String mediaFileName = "videoplayback2.mp4";

    // Indexer processor configuration parameters
    private  String indexerProcessorName = "Azure Media Indexer";
    private  String indexerTaskPresetTemplateFileName = "indexerTaskPresetTemplate.xml";
    private  String title = "";
    private  String description = "";
    private  String language = "English";
    private  String captionFormats = "ttml;sami;webvtt";
    private  String generateAIB = "true";
    private  String generateKeywords = "true";

    // Destination path
    private  String destinationPath = "IndexerOutput";

    public static void main(String...args){
        new VideoIndexer().index("AppleWrite.mp4", "Azure Media Hyperlapse","");
    }

    @Override
    public IndexingResult index(String filePath, String service, String persistPath) {
        LogQueue.INSTANCE.enqueue("Running Indexing Service: "+service);
        System.err.println("PATH: "+filePath);
        mediaFileName = filePath;
        indexerProcessorName = service;
        indexerTaskPresetTemplateFileName = getPresetTemplatePath(service);
        destinationPath = getDestinationPath(service);

        //checking emotion service
        if(service.equals("Azure Media Emotion Detector"))
            indexerProcessorName = "Azure Media Face Detector";

        System.out.println("Preset : "+indexerTaskPresetTemplateFileName +
                           "\nDestination : " + destinationPath+
                           "\nservice" + indexerProcessorName);

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            // Connect to Media Services API with service principal and client symmetric key
            AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
                    tenant,
                    new AzureAdClientSymmetricKey(clientId, clientKey),
                    AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);

            TokenProvider provider = new AzureAdTokenProvider(credentials, executorService);

            // create a new configuration with the new credentials
            Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
                    new URI(restApiEndpoint),
                    provider);

            // create the media service provisioned with the new configuration
            mediaService = MediaService.create(configuration);

            System.out.println("Azure SDK for Java - Media Analytics Sample (Indexer)");

            // Upload a local file to an Asset
            AssetInfo sourceAsset = uploadFileAndCreateAsset(mediaFileName);
            System.out.println("Uploaded AssSet Id: " + sourceAsset.getId());
            IndexingLogQueue.INSTANCE.enqueue("Uploaded AssSet Id: " + sourceAsset.getId());

            // Create indexing task configuration based on parameters
            String indexerTaskPresetTemplate = new String(Files.readAllBytes(
                    Paths.get(new File(indexerTaskPresetTemplateFileName).toURI())));
            String taskConfiguration = (!indexerProcessorName.equals("Azure Media Indexer"))?indexerTaskPresetTemplate:String.format(indexerTaskPresetTemplate, title, description, language, captionFormats, generateAIB, generateKeywords);
            System.out.println("LOADing Preset: " + taskConfiguration);
            // Run indexing job to generate output asset
            AssetInfo outputAsset = runIndexingJob(sourceAsset, taskConfiguration);
            System.out.println("Output Asset Id: " + outputAsset.getId());
            IndexingLogQueue.INSTANCE.enqueue("Output Asset Id: " + outputAsset.getId());

            // Download output asset files
            downloadAssetFiles(outputAsset, destinationPath);

            // Done
            System.out.println("Sample completed!");
            IndexingLogQueue.INSTANCE.enqueue("Completed");
            IndexingResult result = IndexingResult.successResult();
            result.setUploadedPath(filePath);
            return result;
        } catch (ServiceException se) {
            System.out.println("ServiceException encountered.");
            IndexingLogQueue.INSTANCE.enqueue("Error");
            System.out.println(se.toString());
        } catch (Exception e) {
            System.out.println("Exception encountered.");
            IndexingLogQueue.INSTANCE.enqueue("Error");
            System.out.println(e.toString());
        } finally {
            executorService.shutdown();
        }

        return null;
    }

    private String getPresetTemplatePath(String service) {
        switch(service){
            case "Azure Media Indexer": return "indexingTemplates/indexerTaskPresetTemplate.xml";
            case "Azure Media OCR": return "indexingTemplates/videoOcrTaskPresetTemplate.xml";
            case "Azure Media Video Thumbnails": return "indexingTemplates/videoThumbnailTaskPresetTemplate.xml";
            case "Azure Media Hyperlapse": return "indexingTemplates/videoHyperlapseTaskPresetTemplate.xml";
            case "Azure Media Face Detector": return "indexingTemplates/videoFaceTaskPresetTemplate.xml";
            case "Azure Media Emotion Detector": return "indexingTemplates/videoEmotionTaskPresetTemplate.xml";
            case "Azure Media Motion Detector": return "indexingTemplates/videoMotionTaskPresetTemplate.xml";
            case "Azure Media Content Moderator": return "indexingTemplates/videoContentModeratorTaskPresetTemplate.xml";


        }
        return "indexingTemplates/indexerTaskPresetTemplate.xml";
    }

    private String getDestinationPath(String service){
        String dir = "";
        switch(service){
            case "Azure Media Indexer":
                        dir="/caption"; break;
            case "Azure Media OCR":
                        dir="/ocr"; break;
            case "Azure Media Video Thumbnails":
                        dir="/thumbnail"; break;
            case "Azure Media Hyperlapse":
                        dir="/hyperlapse"; break;
            case "Azure Media Face Detector":
                        dir="/face"; break;
            case "Azure Media Emotion Detector":
                        dir="/face"; break;
            case "Azure Media Motion Detector":
                        dir="/motion"; break;
            case "Azure Media Content Moderator":
                        dir="/content"; break;
        }
        return "IndexerOutput" + dir;
    }

    // Upload a media file to your Media Services account.
    // This code creates an Asset, an AccessPolicy (using Write access) and a
    // Locator, and uses those objects to upload a local file.
    private  AssetInfo uploadFileAndCreateAsset(String fileName)
            throws ServiceException, FileNotFoundException, NoSuchAlgorithmException {
        WritableBlobContainerContract uploader;
        AssetInfo asset;
        AccessPolicyInfo uploadAccessPolicy;
        LocatorInfo uploadLocator = null;

        // Create an empty Asset
        asset = mediaService.create(Asset.create().setName(String.format("Media File %s", fileName)));
        System.out.println("Asset created " + asset.getName());
        IndexingLogQueue.INSTANCE.enqueue("Asset created " + asset.getName());
        // Create an AccessPolicy that provides Write access for 15 minutes
        uploadAccessPolicy = mediaService
                .create(AccessPolicy.create("uploadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.WRITE)));

        // Create a SAS Locator using the AccessPolicy and Asset
        uploadLocator = mediaService
                .create(Locator.create(uploadAccessPolicy.getId(), asset.getId(), LocatorType.SAS));

        // Create the Blob Writer using the Locator
        uploader = mediaService.createBlobWriter(uploadLocator);

        // The local file that will be uploaded to your Media Services account
        InputStream input = new FileInputStream(
                new File("uploads/"+fileName));

        System.out.println("Uploading " + fileName);
        IndexingLogQueue.INSTANCE.enqueue("Uploading " + fileName);
        // Upload the local file to the asset
        uploader.createBlockBlob(fileName, input);

        // Inform Media Services about the uploaded files
        mediaService.action(AssetFile.createFileInfos(asset.getId()));
        System.out.println("Uploaded Asset File " + fileName);
        IndexingLogQueue.INSTANCE.enqueue("Uploaded Asset File " + fileName);


        // Delete the SAS Locator (and Access Policy) for the Asset since we are done uploading files
        mediaService.delete(Locator.delete(uploadLocator.getId()));
        mediaService.delete(AccessPolicy.delete(uploadAccessPolicy.getId()));

        return asset;
    }

    // Create a Job that contains a Task to process the Asset
    private  AssetInfo runIndexingJob(AssetInfo asset, String taskConfiguration)
            throws ServiceException, InterruptedException {
        System.out.println("Starting Indexing Job");
        // Retrieve the list of Media Processors that match the name
        ListResult<MediaProcessorInfo> mediaProcessors = mediaService
                .list(MediaProcessor.list().set("$filter", String.format("Name eq '%s'", indexerProcessorName)));

        // Use the latest version of the Media Processor
        MediaProcessorInfo mediaProcessor = null;
        for (MediaProcessorInfo info : mediaProcessors) {
            if (null == mediaProcessor || info.getVersion().compareTo(mediaProcessor.getVersion()) > 0) {
                mediaProcessor = info;
            }
        }

        System.out.println("Using Media Processor: " + mediaProcessor.getName() + " " + mediaProcessor.getVersion());

        // Create a task with the specified Media Processor
        String outputAssetName = String.format("Indexer Results %s", asset.getName());
        String taskXml = "<taskBody><inputAsset>JobInputAsset(0)</inputAsset>"
                + "<outputAsset assetCreationOptions=\"0\"" // AssetCreationOptions.None
                + " assetName=\"" + outputAssetName + "\">JobOutputAsset(0)</outputAsset></taskBody>";

        Task.CreateBatchOperation task = Task.create(mediaProcessor.getId(), taskXml)
                .setConfiguration(taskConfiguration).setName("Indexing Task");

        // Create the Job; this automatically schedules and runs it.
        Job.Creator jobCreator = Job.create()
                .setName("Indexing Job")
                .addInputMediaAsset(asset.getId()).setPriority(0).addTaskCreator(task);
        JobInfo job = mediaService.create(jobCreator);

        String jobId = job.getId();
        System.out.println("Created Job with Id: " + jobId);
        IndexingLogQueue.INSTANCE.enqueue("Created Job with Id: " + jobId);

        // Check to see if the Job has completed
        JobState result = checkJobStatus(jobId);

        // Done with the Job
        if (result != JobState.Finished) {
            System.out.println("The job has finished with a wrong status: " + result.toString());
            throw new RuntimeException();
        }

        System.out.println("Job Finished!");

        IndexingLogQueue.INSTANCE.enqueue("Job Finished!");
        // Get the output Asset
        ListResult<AssetInfo> outputAssets = mediaService.list(Asset.list(job.getOutputAssetsLink()));
        AssetInfo outputAsset = outputAssets.get(0);

        System.out.println("Output asset: " + outputAsset.getName());
        IndexingLogQueue.INSTANCE.enqueue("Output asset: " + outputAsset.getName());
        return outputAsset;
    }

    private  JobState checkJobStatus(String jobId) throws InterruptedException, ServiceException {
        boolean done = false;
        JobState jobState = null;
        while (!done) {
            // Sleep for 5 seconds
            Thread.sleep(5000);

            // Query the updated Job state
            jobState = mediaService.get(Job.get(jobId)).getState();
            System.out.println("Job state: " + jobState);
            IndexingLogQueue.INSTANCE.enqueue(jobState.toString());

            if (jobState == JobState.Finished || jobState == JobState.Canceled || jobState == JobState.Error) {
                done = true;
                IndexingLogQueue.INSTANCE.enqueue("DONE");

            }
        }

        return jobState;
    }

    private  void downloadAssetFiles(AssetInfo asset, String destinationPath) throws ServiceException, IOException {
        // Create destination directory if does not exist
        new File(destinationPath).mkdir();

        // Create an AccessPolicy that provides Read access for 15 minutes
        AccessPolicyInfo accessPolicy = mediaService
                .create(AccessPolicy.create("downloadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.READ)));

        // Create a SAS Locator using the AccessPolicy and Asset
        LocatorInfo sasLocator = mediaService
                .create(Locator.create(accessPolicy.getId(), asset.getId(), LocatorType.SAS));

        // List all the Asset Files
        ListResult<AssetFileInfo> assetFiles = mediaService.list(AssetFile.list(asset.getAssetFilesLink()));

        for (AssetFileInfo file : assetFiles) {
            System.out.print("Downloading " + file.getName() + " output file...");

            URL downloadUrl = new URL(sasLocator.getBaseUri() + "/" + file.getName() + sasLocator.getContentAccessToken());
            HttpURLConnection httpConn = (HttpURLConnection) downloadUrl.openConnection();
            InputStream inputStream = httpConn.getInputStream();
            Files.copy(inputStream, Paths.get(destinationPath, file.getName()), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            System.out.println("Done!");
        }

        // Clean up Locator and Access Policy
        mediaService.delete(Locator.delete(sasLocator.getId()));
        mediaService.delete(AccessPolicy.delete(accessPolicy.getId()));
    }
}
