package com.example.miwatch.opengl;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public final class MainActivity extends Activity {

	private GLSurfaceView mGLSurfaceView;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setPreserveEGLContextOnPause(true);
		mGLSurfaceView.setRenderer(new SimpleRenderer(getApplicationContext()));
		setContentView(mGLSurfaceView);
	}

	@Override
	public void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}
}