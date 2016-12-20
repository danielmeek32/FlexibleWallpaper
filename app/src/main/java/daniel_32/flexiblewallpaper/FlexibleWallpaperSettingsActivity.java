package daniel_32.flexiblewallpaper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.InputStream;
import java.io.OutputStream;

public class FlexibleWallpaperSettingsActivity extends Activity
{
	private static final int SELECT_PICTURE_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_PICTURE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_PICTURE_REQUEST)
		{
			if (resultCode == RESULT_OK && data != null && data.getData() != null)
			{
				try
				{
					Uri picture_uri = data.getData();
					InputStream picture_input_stream = getContentResolver().openInputStream(picture_uri);
					OutputStream picture_output_stream = openFileOutput("picture", MODE_PRIVATE);
					byte[] buffer = new byte[512];
					int length;
					while ((length = picture_input_stream.read(buffer)) >= 0)
					{
						picture_output_stream.write(buffer, 0, length);
					}
					picture_output_stream.close();
					picture_input_stream.close();

					for (ActivityManager.RunningServiceInfo service : ((ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE))
					{
						if (service.service.getClassName().equals(FlexibleWallpaperService.class.getName()))
						{
							FlexibleWallpaperService.onPictureChanged();
						}
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}
		}
		finish();
	}
}