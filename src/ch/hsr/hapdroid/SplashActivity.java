package ch.hsr.hapdroid;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.SplashScene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.bitmap.BitmapTexture.BitmapTextureFormat;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import android.content.Intent;
import android.graphics.Rect;

/**\class SplashActivity 
 * Activity is used to get the size of the visible application frame.
 * This class is used as a workaround to fix issues with AndEngine if it's not used full screen.
 * It also fixes possible issues with soft keys on Android version 4.0 and tablets.
 * @author Remo Egli
 *
 */
public class SplashActivity extends BaseGameActivity {

	private Camera mCamera;
	private IBitmapTextureAtlasSource mSplashTextureAtlasSource;
	private TextureRegion mLoadingScreenTextureRegion;
	private static final float SPLASH_DURATION = 3.0f;
	private static final float SPLASH_SCALE_FROM = 0.9f;
	private static final float SPLASH_SCALE_TO = 1f;
	private Intent intent;
   
	/**
	 * The width and height of the Camera and the RatioResolutionPolicy are taken from the image
	 * to prevent it's distortion.
	 */
	@Override
	public Engine onLoadEngine() {
		// The base path is "assets"
		this.mSplashTextureAtlasSource = new AssetBitmapTextureAtlasSource(this, "gfx/splash.png");
	
		final int width = this.mSplashTextureAtlasSource.getWidth();
		final int height = this.mSplashTextureAtlasSource.getHeight();
	
		this.mCamera = new Camera(0, 0, width, height);
		return new Engine(new EngineOptions(false, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(width, height), this.mCamera));
	}

	@Override
	public void onLoadResources() {
		final BitmapTextureAtlas loadingScreenBitmapTextureAtlas = BitmapTextureAtlasFactory.createForTextureAtlasSourceSize(BitmapTextureFormat.RGBA_8888, this.mSplashTextureAtlasSource, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mLoadingScreenTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromSource(loadingScreenBitmapTextureAtlas, this.mSplashTextureAtlasSource, 0, 0);
		this.getEngine().getTextureManager().loadTexture(loadingScreenBitmapTextureAtlas);
	}

	/**
	 * The size of the visible application frame must be calculated in this method
	 * because it's passed on with the intent extras to the HAPdroidGraphletActivity
	 * which is held in the scenes UpdateHandler. 
	 */
	@Override
	public Scene onLoadScene() {
		final SplashScene splashScene = new SplashScene(this.mCamera, this.mLoadingScreenTextureRegion, SPLASH_DURATION, SPLASH_SCALE_FROM, SPLASH_SCALE_TO);
		splashScene.setBackground(new ColorBackground(0, 0, 0));
		
		Rect rect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			
		intent = new Intent(SplashActivity.this, HAPdroidGraphletActivity.class);
	    intent.putExtra("screenWidth", rect.width());
	    intent.putExtra("screenHeight", rect.height());
		
		splashScene.registerUpdateHandler(new TimerHandler(SPLASH_DURATION, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				SplashActivity.this.startActivity(intent);
				SplashActivity.this.finish();
			}
		}));
	
		return splashScene;
	}

	@Override
	public void onLoadComplete() {
	}

}
