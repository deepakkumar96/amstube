package com.assignment.amstube.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import com.microsoft.windowsazure.services.media.authentication.TokenProvider;
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

public final class Program {

    private static MediaContract mediaService;

    // Media Services account credentials configuration
    private static String tenant = "d019bba4-a1af-4540-9b28-260c46d770a5";
    private static String clientId = "72e751af-b8b2-4a36-a46c-f73fab8f91f9";
    private static String clientKey = "XIdihi+WmfGQ9SPwnEgfgnjWjao++iYpR/XN9Sb5C9w=";
    private static String restApiEndpoint = "https://mediatest.restv2.centralindia.media.azure.net/api/";
    // Input file
    private static String mediaFileName = "AppleWrite.mp4";

    // Indexer processor configuration parameters
    private static String indexerProcessorName = "Azure Media Indexer";
    private static String indexerTaskPresetTemplateFileName = "indexerTaskPresetTemplate.xml";
    private static String title = "";
    private static String description = "";
    private static String language = "English";
    private static String captionFormats = "ttml;sami;webvtt";
    private static String generateAIB = "true";
    private static String generateKeywords = "true";

    // Destination path
    private static String destinationPath = "IndexerOutput";

    // Utility classes should not have a public or default constructor
    private Program() {
    }

    public static void main(String[] args){
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
            System.out.println("Uploaded Asset Id: " + sourceAsset.getId());

            // Create indexing task configuration based on parameters
            String indexerTaskPresetTemplate = new String(Files.readAllBytes(
                    Paths.get(new File("/home/deepak/Documents/amstube/src/main/resources/"+indexerTaskPresetTemplateFileName).toURI())));
            String taskConfiguration = String.format(indexerTaskPresetTemplate, title, description, language, captionFormats, generateAIB, generateKeywords);
            System.out.println(taskConfiguration);
            // Run indexing job to generate output asset
            AssetInfo outputAsset = runIndexingJob(sourceAsset, taskConfiguration);
            System.out.println("Output Asset Id: " + outputAsset.getId());

            // Download output asset files
            downloadAssetFiles(outputAsset, destinationPath);

            // Done
            System.out.println("Sample completed!");

        } catch (ServiceException se) {
            System.out.println("ServiceException encountered.");
            System.out.println(se.toString());
        } catch (Exception e) {
            System.out.println("Exception encountered.");
            System.out.println(e.toString());
        } finally {
            executorService.shutdown();
        }
    }

    // Upload a media file to your Media Services account.
    // This code creates an Asset, an AccessPolicy (using Write access) and a
    // Locator, and uses those objects to upload a local file.
    private static AssetInfo uploadFileAndCreateAsset(String fileName)
            throws ServiceException, FileNotFoundException, NoSuchAlgorithmException {
        WritableBlobContainerContract uploader;
        AssetInfo asset;
        AccessPolicyInfo uploadAccessPolicy;
        LocatorInfo uploadLocator = null;

        // Create an empty Asset
        asset = mediaService.create(Asset.create().setName(String.format("Media File %s", fileName)));
        System.out.println("Asset created " + asset.getName());

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
                new File("/home/deepak/Documents/amstube/src/main/resources/"+fileName));

        System.out.println("Uploading " + fileName);

        // Upload the local file to the asset
        uploader.createBlockBlob(fileName, input);

        // Inform Media Services about the uploaded files
        mediaService.action(AssetFile.createFileInfos(asset.getId()));
        System.out.println("Uploaded Asset File " + fileName);

        // Delete the SAS Locator (and Access Policy) for the Asset since we are done uploading files
        mediaService.delete(Locator.delete(uploadLocator.getId()));
        mediaService.delete(AccessPolicy.delete(uploadAccessPolicy.getId()));

        return asset;
    }

    // Create a Job that contains a Task to process the Asset
    private static AssetInfo runIndexingJob(AssetInfo asset, String taskConfiguration)
            throws ServiceException, InterruptedException {
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

        // Check to see if the Job has completed
        JobState result = checkJobStatus(jobId);

        // Done with the Job
        if (result != JobState.Finished) {
            System.out.println("The job has finished with a wrong status: " + result.toString());
            throw new RuntimeException();
        }

        System.out.println("Job Finished!");

        // Get the output Asset
        ListResult<AssetInfo> outputAssets = mediaService.list(Asset.list(job.getOutputAssetsLink()));
        AssetInfo outputAsset = outputAssets.get(0);

        System.out.println("Output asset: " + outputAsset.getName());

        return outputAsset;
    }

    private static JobState checkJobStatus(String jobId) throws InterruptedException, ServiceException {
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

        return jobState;
    }

    private static void downloadAssetFiles(AssetInfo asset, String destinationPath) throws ServiceException, IOException {
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