package tech.nadlan.com.nadlanproject.Adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tech.nadlan.com.nadlanproject.Models.FileMedia;
import tech.nadlan.com.nadlanproject.R;

/**
 * Created by מוחמד on 01/06/2018.
 */

public class FilesAdapter extends BaseAdapter {
    Context context;
    List<FileMedia> fileList;


    public FilesAdapter(Context context, List<FileMedia> fileList)
    {
        this.context = context;
        this.fileList = fileList;
    }



    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.file_item_layout, null);
            TextView fileNameTv = (TextView)convertView.findViewById(R.id.fileName_tv);
            TextView downloadTv = (TextView)convertView.findViewById(R.id.downloadTv);
            TextView deleteT = (TextView)convertView.findViewById(R.id.downloadTv);
            fileNameTv.setText(fileList.get(position).getFileName());
            downloadTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "הקובץ בהורדה..", Toast.LENGTH_SHORT).show();
                    DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri =   Uri.parse(fileList.get(position).getPath());
                    DownloadManager.Request request=new DownloadManager.Request(uri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    Long reference=downloadManager.enqueue(request);
                }
            });
        }



        return convertView;
    }
}
