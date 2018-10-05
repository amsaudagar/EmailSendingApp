package mvp.android.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mvp.android.filechooser.presenter.IMainPresenter;
import mvp.android.filechooser.presenter.MainPresenter;
import mvp.android.filechooser.view.IMainView;

/**
 * Represents the main activity class
 */
public class MainActivity extends Activity implements OnClickListener, IMainView {

    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 1;

    private static final int REQUEST_PICK_FIRST_FILE = 1;
    private static final int REQUEST_PICK_SECOND_FILE = 2;

    private static final String DIRECTORY_NAME = "Anagram";
    private static final String FILE_NAME = "anagram.txt";

    private TextView mFirstFileSelected;
    private TextView mSecondFileSelected;

    private Button mGenerateFile;

    private List<String> mFirstFileLines;
    private List<String> mSecondFileLines;

    private boolean mIsFirstFileSelected;
    private boolean mIsSecondFileSelected;

    private Uri mUri;

    private IMainPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeResources();

        requestRuntimePermission();
    }

    /**
     * Initializes the resources
     */
    private void initializeResources() {
        mFirstFileSelected = findViewById(R.id.first_file_selected);
        mSecondFileSelected = findViewById(R.id.second_file_selected);

        findViewById(R.id.select_first_file).setOnClickListener(this);
        findViewById(R.id.select_second_file).setOnClickListener(this);

        mGenerateFile = findViewById(R.id.generate_anagram_file);
        mGenerateFile.setOnClickListener(this);

        mFirstFileLines = new ArrayList<>();
        mSecondFileLines = new ArrayList<>();

        mPresenter = new MainPresenter(this, this);
    }

    /**
     * Requests for the runtime permissions
     */
    private void requestRuntimePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent chooseFile;
        switch (v.getId()) {
            case R.id.select_first_file:
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, getString(R.string.choose_text_file));
                startActivityForResult(chooseFile, REQUEST_PICK_FIRST_FILE);
                break;
            case R.id.select_second_file:
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, getString(R.string.choose_text_file));
                startActivityForResult(chooseFile, REQUEST_PICK_SECOND_FILE);
                break;
            case R.id.generate_anagram_file:
                GenerateAnagramFile generateAnagramFile = new GenerateAnagramFile();
                generateAnagramFile.execute();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_FIRST_FILE:
                    readFile(data.getData(), mFirstFileLines, mFirstFileSelected);
                    mIsFirstFileSelected = true;
                    enableGenerateAnagramFileButton();
                    break;
                case REQUEST_PICK_SECOND_FILE:
                    readFile(data.getData(), mSecondFileLines, mSecondFileSelected);
                    mIsSecondFileSelected = true;
                    enableGenerateAnagramFileButton();
                    break;
            }
        }
    }

    /**
     * Enables the generate file button only if both the file have been selected
     */
    private void enableGenerateAnagramFileButton() {
        mGenerateFile.setEnabled((mIsFirstFileSelected && mIsSecondFileSelected));
    }

    /**
     * Read the file for given uri
     *
     * @param uri             Uri of file
     * @param fileLines       List of lines in file
     * @param txtFileSelected Selected file name view
     */
    private void readFile(Uri uri, List<String> fileLines, TextView txtFileSelected) {
        BufferedReader reader = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                fileLines.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            txtFileSelected.setText(R.string.file_selected);
        }
    }

    /**
     * Class responsible to generate file for anagram lines
     */
    private class GenerateAnagramFile extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog mProgressDialog;
        private List<String> mAnagramLines;
        private String mFileContent = "";

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(MainActivity.this,
                    "ProgressDialog", "Please wait...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            int anagramLineCount = 0;
            List<String> fileLines = new ArrayList<>(mFirstFileLines);
            mAnagramLines = new ArrayList<>();

            for(int i = 0; i < mFirstFileLines.size(); i++) {

                String line1 = mFirstFileLines.get(i);
                line1 = line1.replaceAll("\\s", "");
                char[] arr1 = line1.toLowerCase().toCharArray();
                Arrays.sort(arr1);

                // Avoid empty lines
                if(!line1.equals("")) {
                    for(int j = 0; j < mSecondFileLines.size(); j++) {

                        String line2 = mSecondFileLines.get(j);
                        line2 = line2.replaceAll("\\s", "");

                        char[] arr2 = line2.toLowerCase().toCharArray();
                        Arrays.sort(arr2);

                        if(Arrays.equals(arr1, arr2)) {
                            anagramLineCount++;
                            publishProgress(anagramLineCount);
                            mAnagramLines.add(fileLines.get(i));
                        }
                    }
                }
            }

            writeFileForAnagramLines();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            showAnagramNotification(values[0]);

        }

        @Override
        protected void onPostExecute(Void result) {
            mProgressDialog.dismiss();
            sendEmail();
        }

        /**
         * Created the anagram file and write the anagram lines
         */
        private void writeFileForAnagramLines() {

            for(String line : mAnagramLines) {
                mFileContent = mFileContent + "\n" + line;
            }

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //TODO handle case of no SDCARD present
            } else {
                String dir = Environment.getExternalStorageDirectory() + File.separator + DIRECTORY_NAME;
                File folder = new File(dir);
                folder.mkdirs();
                File file = new File(dir, FILE_NAME);
                try {
                    if(!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(mFileContent.getBytes());
                    fos.close();
                    mUri = Uri.fromFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Sends the mail with attaching the anagram file
         */
        private void sendEmail() {
            try {
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { getString(R.string.email) });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
                if (mUri != null) {
                    emailIntent.putExtra(Intent.EXTRA_STREAM, mUri);
                }
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_body));
                startActivity(Intent.createChooser(emailIntent,getString(R.string.send_mail)));
            } catch (Throwable t) {
                Toast.makeText(MainActivity.this, "Request failed try again: " + t.toString(),Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Shows the notification when anagram line is found
         *
         * @param count - Number of anagram lines found
         */
        private void showAnagramNotification(int count) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(getString(R.string.notification_message, count));

            Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
