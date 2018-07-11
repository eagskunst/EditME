package com.eagsprojects.editordetexo.Handlers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eagsprojects.editordetexo.Fragments.ArchivesFragment;
import com.eagsprojects.editordetexo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emmanuel on 1/4/2018.
 */

public class DownloadHandler extends TaskGenerator implements GoogleApiClient.OnConnectionFailedListener{

    private static final String ARCHIVES_TAG = "ArchivesFragment";
    private static final String TAG = "DownloadHandler";

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private TextFilesHandler filesHandler;
    private Activity activity;
    private List<DriveId> filesDriveId;
    private ArrayList<String> filesTitles;
    private DriveId folderId;
    private Task<DriveContents>[] openFilesTask;
    private ProgressBar progressBar;

    public DownloadHandler(DriveClient driveClient, DriveResourceClient driveResourceClient, TextFilesHandler filesHandler, Activity activity,ProgressBar progressBar) {
        super(driveClient,driveResourceClient,activity);
        this.driveClient = driveClient;
        this.driveResourceClient = driveResourceClient;
        this.filesHandler = filesHandler;
        this.activity = activity;
        this.progressBar = progressBar;
        filesDriveId = new ArrayList<>();
        filesTitles = new ArrayList<>();
    }

    public void startDownload(){
        final Task<MetadataBuffer> queryTask = createQueryTask();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        /*
        When synced and looked for the folder on GoogleDrive success,
        start looking for text files
         */



        queryTask.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });

        Tasks.whenAllSuccess(queryTask)
                .continueWithTask(new Continuation<List<Object>, Task<MetadataBuffer>>() {
                    @Override
                    public Task<MetadataBuffer> then(@NonNull Task<List<Object>> task) throws Exception {
                        folderId = getFolderId();
                        Query query = new Query.Builder()
                                .addFilter(Filters.eq(SearchableField.MIME_TYPE,"text/plain"))
                                .build();
                        /*
                        returning a task to search for text files in a specific folder
                         */
                        return driveResourceClient.queryChildren(folderId.asDriveFolder(),query);
                    }
                })
                .addOnCompleteListener(activity, new OnCompleteListener<MetadataBuffer>() {
            @Override
            public void onComplete(@NonNull Task<MetadataBuffer> task) {
                if (task.isSuccessful()){
                    progressBar.setProgress(50);
                    Log.i(TAG, "Complete ");
                for (Metadata metadata : task.getResult()) {
                    if (!metadata.isTrashed() && metadata.getOriginalFilename().endsWith(".txt")) {
                        /*
                        Adding files to a Task[] array;
                         */
                        filesDriveId.add(metadata.getDriveId());
                        filesTitles.add(metadata.getOriginalFilename());
                    }
                }
                task.getResult().release();
                /*
                An array of Task for opening files, for each task we create a file.
                If we get to the last task, then start a Fragment Transaction.
                 */
                int size = filesTitles.size();
                openFilesTask = new Task[size];
                Log.i(TAG, "open Task number: " + size);
                for (int i = 0; i < openFilesTask.length; i++) {
                    progressBar.setProgress(75);
                    final String title = filesTitles.get(i);
                    openFilesTask[i] = driveResourceClient.openFile(filesDriveId.get(i).asDriveFile(), DriveFile.MODE_READ_ONLY);
                    final int finalI = i;
                    openFilesTask[i]
                            .addOnSuccessListener(activity, new OnSuccessListener<DriveContents>() {
                                @Override
                                public void onSuccess(DriveContents contents) {
                                    // Process contents...
                                    // [START_EXCLUDE]
                                    // [START read_as_string]
                                    try (BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(contents.getInputStream()))) {
                                        StringBuilder builder = new StringBuilder();
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            builder.append(line).append("\n");
                                        }
                                        File file = new File(Environment.getExternalStorageDirectory() + "/Texts", title);
                                        Writer writer = new BufferedWriter(new FileWriter(file));
                                        Log.i(TAG, "Texts: " + builder.toString());
                                        writer.write(builder.toString());
                                        writer.close();
                                        if (file.exists()) {
                                            Log.i(TAG, "It exists. Can write?: " + file.canWrite() + ". Can read?: " + file.canRead());
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if(finalI == (openFilesTask.length -1)){
                                        progressBar.setProgress(100);
                                        Bundle bundle = new Bundle();
                                        ArchivesFragment archivesFragment = new ArchivesFragment();
                                        filesHandler.refreshList();
                                        bundle.putParcelable("File_handler",filesHandler);
                                        archivesFragment.setArguments(bundle);
                                        activity.getFragmentManager().beginTransaction()
                                                .replace(R.id.container,archivesFragment,ARCHIVES_TAG)
                                                .addToBackStack(null)
                                                .commit();
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(activity, "Files downloaded.", Toast.LENGTH_SHORT).show();
                                        filesDriveId.clear();
                                        filesTitles.clear();
                                        activity = null;
                                    }
                                }
                            });
                }
            }
            else{
                    Toast.makeText(activity, "Couldn't find files", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    ArchivesFragment archivesFragment = new ArchivesFragment();
                    filesHandler.refreshList();
                    bundle.putParcelable("File_handler",filesHandler);
                    archivesFragment.setArguments(bundle);
                    activity.getFragmentManager().beginTransaction()
                            .replace(R.id.container,archivesFragment,ARCHIVES_TAG)
                            .addToBackStack(null)
                            .commit();
                    progressBar.setVisibility(View.GONE);
                    queryTask.getResult().release();
                    activity = null;
                }
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(activity, R.string.connection_timed_out+". Error: "+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        activity.findViewById(R.id.recyclerview).setVisibility(View.VISIBLE);
    }
}
