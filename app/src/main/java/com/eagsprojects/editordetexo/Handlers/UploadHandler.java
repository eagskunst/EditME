package com.eagsprojects.editordetexo.Handlers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emmanuel on 29/3/2018.
 * Multiple task that come from TaskGenerator with
 * the objective of uplaoding texts.
 */

public class UploadHandler extends TaskGenerator implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "UploadHandler";

    private DriveClient driveClient;
    private ProgressBar progressBar;
    private DriveResourceClient driveResourceClient;
    private TextFilesHandler filesHandler;
    private Activity activity;
    private DriveId folderId;
    private String[] s;

    public UploadHandler(DriveClient driveClient, DriveResourceClient driveResourceClient, TextFilesHandler filesHandler, Activity activity, ProgressBar progressBar) {
        super(driveClient,driveResourceClient,activity);
        this.driveClient = driveClient;
        this.driveResourceClient = driveResourceClient;
        this.filesHandler = filesHandler;
        this.activity = activity;
        this.progressBar = progressBar;
        startSync();
    }


    private void startSync() {
        Toast.makeText(activity, "Starting sync...", Toast.LENGTH_SHORT).show();//Remember to use a R.string resource here!
        s = filesHandler.getS();
        final Task<DriveContents>[] createContentsTask = new Task[s.length];

        progressBar.setVisibility(View.VISIBLE);

        final Task<Void> syncTask = createSyncTask();
        final Task<MetadataBuffer> queryTask = createQueryTask();

        final ArrayList<String> completeTexts = filesHandler.getCompleteText();
        final ArrayList<String> titles = filesHandler.getTitles();


        Tasks.whenAllSuccess(syncTask,queryTask)
                .addOnSuccessListener(activity, new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {
                        folderId = getFolderId();
                        final Task<MetadataBuffer> deleteTask = createDeleteFilesTask(folderId);
                        deleteTask.addOnCompleteListener(activity, new OnCompleteListener<MetadataBuffer>() {
                            @Override
                            public void onComplete(@NonNull Task<MetadataBuffer> task) {
                                /*
                                For each file, create a Task to upload it.
                                 */
                                for(int i = 0;i<s.length;i++){
                                    createContentsTask[i] = driveResourceClient.createContents();
                                    final String text = completeTexts.get(i);
                                    final String title = titles.get(i);
                                    if(i != (s.length - 1)){
                                        createContentsTask[i].addOnCompleteListener(new OnCompleteListener<DriveContents>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DriveContents> task) {
                                                uploadFiles(task,text,title);
                                            }
                                        });
                                    }
                                    else{
                                        createContentsTask[i].addOnCompleteListener(new OnCompleteListener<DriveContents>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DriveContents> task) {
                                                uploadFiles(task,text,title);
                                            }
                                        });
                                    }
                                }
                                Tasks.whenAllComplete(createContentsTask[((s.length)-1)])
                                        .addOnCompleteListener(activity, new OnCompleteListener<List<Task<?>>>() {
                                            @Override
                                            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show();//Remember to use a R.string resource here!
                                            }
                                        });
                                task.getResult().release();
                            }
                        });


                    }
                });

    }

    private void uploadFiles(Task<DriveContents> createContentsTask,String completeText, String title) {
        DriveFolder parent = folderId.asDriveFolder();
        Log.i(TAG, "Entré!");
        DriveContents contents = createContentsTask.getResult();
        OutputStream outputStream = contents.getOutputStream();
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            writer.write(completeText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(title)
                .setMimeType("text/plain")
                .setStarred(true)
                .build();
        driveResourceClient.createFile(parent, changeSet, contents);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
