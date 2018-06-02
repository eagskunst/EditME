package com.eagsprojects.editordetexo.Handlers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.eagsprojects.editordetexo.R;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

/**
 * Created by Emmanuel on 1/4/2018.
 */

public class TaskGenerator{

    private static final String TAG = "TaskGenerator";


    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private Activity activity;
    public DriveId folderId;


    public TaskGenerator(DriveClient driveClient, DriveResourceClient driveResourceClient,Activity activity){
        this.driveClient = driveClient;
        this.driveResourceClient = driveResourceClient;
        this.activity = activity;
    }

    public Task<Void> createSyncTask() {
        /*
        Start sync task
         */
        final Task<Void> syncTask = driveClient.requestSync();
        syncTask.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG,"Sync fail. "+e);
            }
        });
        syncTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(TAG,"ENTRÉ");
                if(task.isSuccessful()){
                    Log.i(TAG,"SYNC SUCCESS");
                }
                else {
                    Toast.makeText(activity, "Couldn't sync with Google Drive.", Toast.LENGTH_SHORT).show();//Remember to use a R.string resource here!
                    Log.i(TAG, "FAIL SYNC");
                }
            }
        });
        return syncTask;
    }

    public Task<MetadataBuffer> createQueryTask() {
        /*
        Start querytask to look for folder
         */
        Log.i(TAG,"START QUERY FOLDER. ");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE,"EditME Texts"))
                .build();

        final Task<MetadataBuffer> queryTask = driveResourceClient.query(query);

        queryTask.addOnCompleteListener(new OnCompleteListener<MetadataBuffer>() {
            @Override
            public void onComplete(@NonNull Task<MetadataBuffer> task) {
                boolean folderExist = false;

                if(task.isSuccessful()){
                    Log.i(TAG,"SUCCESS FOR BROWSE ");
                    for(Metadata metadata : task.getResult()){
                        if(metadata.isFolder() && !metadata.isTrashed()){
                            Log.i(TAG,"Folder: "+metadata.getTitle());
                            folderExist = true;
                            folderId = metadata.getDriveId();
                            Log.i(TAG,"ID: "+folderId.toString());
                            break;
                        }
                    }
                    if(!folderExist){
                        createFolder();
                    }
                    task.getResult().release();

                }
                else{
                    Toast.makeText(activity, "Couldn't complete the browse.", Toast.LENGTH_SHORT).show();//Remember to use a R.string resource here!
                    Log.i(TAG,"FAIL");

                }
            }
        });
        return queryTask;
    }


    private void createFolder() {
        driveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("EditME Texts")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return driveResourceClient.createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(activity,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                folderId = driveFolder.getDriveId();
                                Log.i(TAG,"Carpeta creada");

                            }
                        })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "No se pudo crear la carpeta", e);
                        Toast.makeText(activity, "Couldn't create the folder.", Toast.LENGTH_SHORT).show();//Remember to use a R.string resource here!
                    }
                });
    }

    public Task<MetadataBuffer> createDeleteFilesTask(DriveId folderId){
        Log.i(TAG,"START QUERY FOLDER. ");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE,"text/plain"))
                .build();


        Task<MetadataBuffer> queryTask = driveResourceClient
                .queryChildren(folderId.asDriveFolder(),query);
        /*
        Start queryTask for deleting files that have the same name on our
        Google Drive. Requiered for no creating same name files with differents
        content since GD doesn't overwrite files.
         */
        queryTask.addOnCompleteListener(activity, new OnCompleteListener<MetadataBuffer>() {
            @Override
            public void onComplete(@NonNull Task<MetadataBuffer> task) {
                File root = new File(Environment.getExternalStorageDirectory().toString()+"/Texts");
                String s[] = root.list();
                for (Metadata metadata:task.getResult()) {
                    for (int i = 0;i<s.length;i++){
                        if(s[i].equals(metadata.getTitle())){
                            driveResourceClient.delete(metadata.getDriveId().asDriveFile());
                        }
                    }
                }
            }
        });

        return  queryTask;
    }


    public DriveId getFolderId() {
        return folderId;
    }

}
