package com.kimyoung;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import java.io.File;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class RadioMapServer extends Activity {
    private RadioMap2 rmIndoor;

    private final int DefaultNaNValue = -110;

    private String folder_path;
    private String indoorFolder;
    private String indoorRSSFolder;
    private String indoorFilename ;
    private String indoorTestData;
    private SharedPreferences sharedPreferences;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(RSSLogger.SHARED_PREFS_NAME, MODE_PRIVATE);

        folder_path = sharedPreferences.getString("folder_browser", "");

        indoorFolder = folder_path + "/indoor";
        indoorRSSFolder = folder_path ;
        indoorFilename = indoorFolder + "/indoor-radiomap.txt";
        indoorTestData = indoorFolder + "/test-data.txt";

        createServerFolders();

        generate_RadioMap();
    }

    public boolean Create_Indoor_Radiomap_Parameters() {
        File folderIndoor = new File(indoorRSSFolder);
        rmIndoor = new RadioMap2(folderIndoor, indoorFilename, DefaultNaNValue);

        if (!rmIndoor.writeParameters(indoorTestData)) {
           TipDialog("There was a problem creating indoor parameters.\n"
                    + "Existed indoor parameters will be used if exist!");
            return false;
        }
        else {
            TipDialog("Created new indoor parameters!");
        }
        return true;
    }

    public boolean Create_Indoor_Radiomap() {
        File folderIndoor = new File(indoorRSSFolder);
        if (folderIndoor.exists() && folderIndoor.canRead() && folderIndoor.isDirectory()) {
            rmIndoor = new RadioMap2(folderIndoor, indoorFilename, DefaultNaNValue);
            if (!rmIndoor.createRadioMap()) {
                TipDialog("There was a problem creating the indoor radio map.\n"
                        + "Existed Indoor Radio Map will be used if exists!");
                return false;
            }
            else {
                TipDialog("Created new indoor Radio Map!");
                return true;
            }
        }
        else {
           TipDialog(indoorRSSFolder + " folder does not exist. Restart Server");
        }
        return false;

    }

    public void createServerFolders() {
        File f = null;
        boolean success = false;

        // Create directory for indoor radiomap, radiomap mean, parameters, rbf-weights
        f = new File(indoorFolder);
        if (!f.exists() || !f.isDirectory()) {
            success = f.mkdir();
            if (!success) {
                TipDialog("Could not create indoor folder!\nChange directory of server");
                System.exit(1);
            }
        }

        // Create directory for indoor RSS files
        f = new File(indoorRSSFolder);
        if (!f.exists() || !f.isDirectory()) {
            success = f.mkdir();
            if (!success) {
                TipDialog("Could not create indoor folder for RSS log files!\nChange directory of server");
                System.exit(1);
            }
        }


    }

    private void generate_RadioMap(){

//        TipDialog("It needs a long time,please wait！");

        progressDialog = ProgressDialog.show(RadioMapServer.this, "", "Generating. Please wait...", true, false);

        if(!Create_Indoor_Radiomap() )
        {
            progressDialog.dismiss();
//            dialog_disclose.dismiss();
            TipDialog("Failed to Create Radiomap！");
            return;
        }
        if(!CopySdcardFile(indoorFilename,indoorTestData))
        {
            progressDialog.dismiss();
//            dialog_disclose.dismiss();
            TipDialog("Failed to Create Testdata！");
            return;
        }

        if(!Create_Indoor_Radiomap_Parameters() )
        {
            progressDialog.dismiss();
//            dialog_disclose.dismiss();
            TipDialog("Failed to Create Parameters！");
            return;
        }

        progressDialog.dismiss();

//        dialog_disclose.dismiss();
        TipDialog("All Created Successfully！");

    }

    public void TipDialog(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tip");//标题
        builder.setMessage(str);
        builder.setIcon(R.drawable.icon1);
        builder.create();
        builder.show();
//        dialog_disclose = builder.show();
    }

    public boolean CopySdcardFile(String fromFile, String toFile)
    {

        try
        {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0)
            {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;

        } catch (Exception ex)
        {
            return false;
        }
    }

}
