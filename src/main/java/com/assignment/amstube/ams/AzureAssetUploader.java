package com.assignment.amstube.ams;

import java.io.*;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.assignment.amstube.models.StreamingVideo;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import com.microsoft.windowsazure.services.media.authentication.AzureEnvironments;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;
import com.microsoft.windowsazure.services.media.models.Task;

public class AzureAssetUploader
{
    // Media Services account credentials configuration
    // Media Services account credentials configuration
    private  static  String tenant = "2cb270e2-de00-4b6e-9586-41c025816210";
    private  static  String clientId = "de8387b2-0a4b-4cfc-b24b-6bffb1b50c57";
    private  static  String clientKey = "2D5PGa55UMMnxHDwMhDXeVA3IWH63IpwmzppoIURNvI=";
    private  static  String restApiEndpoint = "https://mediatest4.restv2.eastus.media.azure.net/api/";

    private static String fileName = "/home/deepak/Downloads/videoplayback.mp4";
    // Media Services API
    private static MediaContract mediaService;

    // Encoder configuration
    // This is using the default Adaptive Streaming encoding preset. 
    // You can choose to use a custom preset, or any other sample defined preset. 
    // In addition you can use other processors, like Speech Analyzer, or Redactor if desired.
    private static String preferredEncoder = "Media Encoder Standard";
    private static String encodingPreset = "H264 Multiple Bitrate 720p";//"Adaptive Streaming";

    public static void main(String[] args){
         upload("uploads/AppleWrite.mp4", "Adaptive Streaming");
    }

    public static StreamingVideo  upload(String filename, String preset)
    {
    	fileName = "uploads/"+filename;
    	encodingPreset = preset;
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            // Setup Azure AD Service Principal Symmetric Key Credentials
            AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
                    tenant,
                    new AzureAdClientSymmetricKey(clientId, clientKey),
                    AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);

            AzureAdTokenProvider provider = new AzureAdTokenProvider(credentials, executorService);

            // Create a new configuration with the credentials
            Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
                    new URI(restApiEndpoint),
                    provider);

            // Create the media service provisioned with the new configuration
            mediaService = MediaService.create(configuration);

            // Upload a local file to an Asset
            AssetInfo uploadAsset = uploadFileAndCreateAsset(filename, fileName);
            System.out.println("Uploaded Asset Id: " + uploadAsset.getId());

            // Transform the Asset
            AssetInfo encodedAsset = encode(uploadAsset);
            System.out.println("Encoded Asset Id: " + encodedAsset.getId());

            // Create the Streaming Origin Locator
            String url = getStreamingOriginLocator(encodedAsset);
            String videoname = uploadAsset.getName();
            int dotLen = filename.lastIndexOf(".");
            int slashLen = filename.lastIndexOf("/") ;
            System.err.println(dotLen + ", " + slashLen);
            StreamingVideo vid = new StreamingVideo(uploadAsset.getId(), filename.substring(slashLen+1, dotLen));
            try{
                vid.setType(videoname.substring(videoname.lastIndexOf("."+1)));
            }catch(Exception ex){
            }
            vid.setStreamingEndpoint(url);
            
          
            
            System.out.println("Origin Locator URL: " + url);
            System.out.println("Sample completed!");
            return vid;

        } catch (ServiceException se) {
            System.out.println("ServiceException encountered.");
            System.out.println(se.toString());
            return null;
        } catch (Exception e) {
            System.out.println("Exception encountered.");
            System.out.println(e.toString());
            return null;
        } finally {
        	
            executorService.shutdown();
        }
    }

    private static AssetInfo uploadFileAndCreateAsset(String assetName, String fileName)
        throws ServiceException, FileNotFoundException, NoSuchAlgorithmException {

        WritableBlobContainerContract uploader;
        AssetInfo resultAsset;
        AccessPolicyInfo uploadAccessPolicy;
        LocatorInfo uploadLocator = null;

        // Create an Asset
        resultAsset = mediaService.create(Asset.create().setName(assetName).setAlternateId("altId"));
        System.out.println("Created Asset " + fileName);

        // Create an AccessPolicy that provides Write access for 15 minutes
        uploadAccessPolicy = mediaService
            .create(AccessPolicy.create("uploadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.WRITE)));

        // Create a Locator using the AccessPolicy and Asset
        uploadLocator = mediaService
            .create(Locator.create(uploadAccessPolicy.getId(), resultAsset.getId(), LocatorType.SAS));

        // Create the Blob Writer using the Locator
        uploader = mediaService.createBlobWriter(uploadLocator);

        File file = new File(fileName);

        // The local file that will be uploaded to your Media Services account
        InputStream input = new FileInputStream(file);

        System.out.println("Uploading " + fileName);

        // Upload the local file to the media asset
        uploader.createBlockBlob(file.getName(), input);

        // Inform Media Services about the uploaded files
        mediaService.action(AssetFile.createFileInfos(resultAsset.getId()));
        System.out.println("Uploaded Asset File " + fileName);

        mediaService.delete(Locator.delete(uploadLocator.getId()));
        mediaService.delete(AccessPolicy.delete(uploadAccessPolicy.getId()));

        return resultAsset;
    }

    // Create a Job that contains a Task to transform the Asset
    private static AssetInfo encode(AssetInfo assetToEncode)
        throws ServiceException, InterruptedException {

        // Retrieve the list of Media Processors that match the name
        ListResult<MediaProcessorInfo> mediaProcessors = mediaService
                        .list(MediaProcessor.list().set("$filter", String.format("Name eq '%s'", preferredEncoder)));

        // Use the latest version of the Media Processor
        MediaProcessorInfo mediaProcessor = null;
        for (MediaProcessorInfo info : mediaProcessors) {
            if (null == mediaProcessor || info.getVersion().compareTo(mediaProcessor.getVersion()) > 0) {
                mediaProcessor = info;
            }
        }

        System.out.println("Using Media Processor: " + mediaProcessor.getName() + " " + mediaProcessor.getVersion());

        // Create a task with the specified Media Processor
        String outputAssetName = String.format("%s as %s", assetToEncode.getName(), encodingPreset);
        String taskXml = "<taskBody><inputAsset>JobInputAsset(0)</inputAsset>"
                + "<outputAsset assetCreationOptions=\"0\"" // AssetCreationOptions.None
                + " assetName=\"" + outputAssetName + "\">JobOutputAsset(0)</outputAsset></taskBody>";

        Task.CreateBatchOperation task = Task.create(mediaProcessor.getId(), taskXml)
                .setConfiguration(encodingPreset).setName("Encoding");

        // Create the Job; this automatically schedules and runs it.
        Job.Creator jobCreator = Job.create()
                .setName(String.format("Encoding %s to %s", assetToEncode.getName(), encodingPreset))
                .addInputMediaAsset(assetToEncode.getId()).setPriority(2).addTaskCreator(task);
        JobInfo job = mediaService.create(jobCreator);

        String jobId = job.getId();
        System.out.println("Created Job with Id: " + jobId);

        // Check to see if the Job has completed
        checkJobStatus(jobId);
        // Done with the Job

        // Retrieve the output Asset
        ListResult<AssetInfo> outputAssets = mediaService.list(Asset.list(job.getOutputAssetsLink()));
        return outputAssets.get(0);
    }


    public static String getStreamingOriginLocator(AssetInfo asset) throws ServiceException {
        // Get the .ISM AssetFile
        ListResult<AssetFileInfo> assetFiles = mediaService.list(AssetFile.list(asset.getAssetFilesLink()));
        AssetFileInfo streamingAssetFile = null;
        for (AssetFileInfo file : assetFiles) {
            if (file.getName().toLowerCase().endsWith(".ism")) {
                streamingAssetFile = file;
                break;
            }
        }

        AccessPolicyInfo originAccessPolicy;
        LocatorInfo originLocator = null;

        // Create a 30-day read only AccessPolicy
        double durationInMinutes = 60 * 24 * 30;
        originAccessPolicy = mediaService.create(
                AccessPolicy.create("Streaming policy", durationInMinutes, EnumSet.of(AccessPolicyPermission.READ)));

        // Create a Locator using the AccessPolicy and Asset
        originLocator = mediaService
                .create(Locator.create(originAccessPolicy.getId(), asset.getId(), LocatorType.OnDemandOrigin));

        // Create a Smooth Streaming base URL
        return originLocator.getPath() + streamingAssetFile.getName() + "/manifest";
    }

    private static void checkJobStatus(String jobId) throws InterruptedException, ServiceException {
        boolean done = false;
        JobState jobState = null;
        while (!done) {
            // Sleep for 5 seconds
            Thread.sleep(5000);

            // Query the updated Job state
            jobState = mediaService.get(Job.get(jobId)).getState();
            System.out.println("Job state: " + jobState);

            if (jobState == JobState.Finished || jobState == JobState.Canceled || jobState == JobState.Error) {
                done = true;
            }
        }
    }
}