package daniel_32.flexiblewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.ArrayList;

public class FlexibleWallpaperService extends WallpaperService
{
	private static ArrayList<FlexibleWallpaperService> services = new ArrayList<>();
	private ArrayList<FlexibleWallpaperEngine> engines = new ArrayList<>();

	protected static void onPictureChanged()
	{
		for (FlexibleWallpaperService service : services)
		{
			service.onPictureChangedInstance();
		}
	}

	private void onPictureChangedInstance()
	{
		for (FlexibleWallpaperEngine engine : engines)
		{
			engine.onPictureChanged();
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		services.add(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		services.remove(this);
	}

	@Override
	public Engine onCreateEngine()
	{
		return new FlexibleWallpaperEngine();
	}

	protected class FlexibleWallpaperEngine extends WallpaperService.Engine
	{
		private Bitmap imageOriginal = null;
		private Bitmap imageScaled = null;
		private int imageWidth;
		private int imageHeight;
		private boolean imageVertical;
		private float imageOffset = 0;

		private SurfaceHolder surfaceHolder = null;
		private int screenWidth = 0;
		private int screenHeight = 0;

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			load_image();
			FlexibleWallpaperService.this.engines.add(this);
		}

		@Override
		public void onDestroy()
		{
			super.onDestroy();
			FlexibleWallpaperService.this.engines.remove(this);
			if (this.imageScaled != null)
			{
				this.imageScaled.recycle();
			}
			if (this.imageOriginal != null)
			{
				this.imageOriginal.recycle();
			}
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			this.imageOffset = xOffset;
			redraw();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			super.onSurfaceChanged(holder, format, width, height);
			this.surfaceHolder = holder;
			this.screenWidth = width;
			this.screenHeight = height;
			scale_image();
		}

		@Override
		public void onSurfaceRedrawNeeded(SurfaceHolder holder)
		{
			super.onSurfaceRedrawNeeded(holder);
			redraw();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder)
		{
			super.onSurfaceDestroyed(holder);
			this.surfaceHolder = null;
		}

		private void load_image()
		{
			if (this.imageOriginal != null)
			{
				this.imageOriginal.recycle();
				this.imageOriginal = null;
			}

			try
			{
				this.imageOriginal = BitmapFactory.decodeFile(getFilesDir() + "/picture");
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}

		private void scale_image()
		{
			if (this.imageScaled != null)
			{
				this.imageScaled.recycle();
				this.imageScaled = null;
			}

			if (this.imageOriginal != null)
			{
				if (this.imageOriginal.isRecycled())
				{
					load_image();
				}

				int width = (int) (((float) this.screenHeight / (float) this.imageOriginal.getHeight()) * this.imageOriginal.getWidth());
				int height = this.screenHeight;
				if (width < this.screenWidth)
				{
					width = this.screenWidth;
					height = (int) (((float) this.screenWidth / (float) this.imageOriginal.getWidth()) * this.imageOriginal.getHeight());
					this.imageVertical = true;
				}
				else
				{
					this.imageVertical = false;
				}

				this.imageScaled = Bitmap.createScaledBitmap(this.imageOriginal, width, height, true);
			}
			else
			{
				this.imageScaled = Bitmap.createBitmap(this.screenWidth, this.screenHeight, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(this.imageScaled);
				canvas.drawColor(Color.BLACK);
				this.imageVertical = false;
			}
			this.imageWidth = this.imageScaled.getWidth();
			this.imageHeight = this.imageScaled.getHeight();
		}

		private void redraw()
		{
			if (this.surfaceHolder != null && this.imageScaled != null)
			{
				if (this.imageScaled.isRecycled())
				{
					scale_image();
				}

				Surface surface = this.surfaceHolder.getSurface();
				Canvas canvas = surface.lockCanvas(this.surfaceHolder.getSurfaceFrame());
				if (this.imageVertical)
				{
					canvas.drawBitmap(this.imageScaled, 0, -(this.imageOffset * (this.imageHeight - this.screenHeight)), null);
				}
				else
				{
					canvas.drawBitmap(this.imageScaled, -(this.imageOffset * (this.imageWidth - this.screenWidth)), 0, null);
				}
				surface.unlockCanvasAndPost(canvas);
			}
		}

		private void onPictureChanged()
		{
			load_image();
			scale_image();
			redraw();
		}
	}
}