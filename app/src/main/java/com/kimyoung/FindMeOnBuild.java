/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * Υou should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.kimyoung;

import java.util.Observable;
import java.util.Observer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.kimyoung.zoom.ClickPoint;
import com.kimyoung.zoom.DynamicZoomControl;
import com.kimyoung.zoom.ImageZoomView2;
import com.kimyoung.zoom.LongPressZoomListener;

import android.graphics.Canvas;
import android.graphics.Path;

public class FindMeOnBuild implements Observer {

	private final ClickPoint curClick = new ClickPoint(-1, -1);
	private String imagePath;
	private String building_width;
	private String building_height;

	/** Image zoom view */
	private ImageZoomView2 mZoomView;

	/** Zoom control */
	private DynamicZoomControl mZoomControl;

	/** Decoded bitmap image */
	private Bitmap mBitmap;

	/** On touch listener for zoom view */
	private LongPressZoomListener mZoomListener;

	private BooleanObservable trackMe;

	private int x_temp = 0;
	private int y_temp = 0;
	private Canvas canvas = new Canvas();
	private Path mPath = new Path();
	
	public FindMeOnBuild(FindMe fm) {

		// Set Zooming and Panning Settings
		mZoomControl = new DynamicZoomControl();
		mZoomListener = new LongPressZoomListener(fm.getApplicationContext());
		mZoomListener.setZoomControl(mZoomControl);

		mZoomView = (ImageZoomView2) fm.findViewById(R.id.zoomview);
		mZoomView.setZoomState(mZoomControl.getZoomState());
		mZoomView.setOnTouchListener(mZoomListener);
		mZoomView.setCurClick(curClick);
		
		mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
	}

	public void setTrackMe(BooleanObservable trackMe) {
		mZoomView.setTrackMe(trackMe);
		
		if (this.trackMe != null) {
			this.trackMe.deleteObserver(this);
		}
		this.trackMe = trackMe;
		this.trackMe.addObserver(this);
	}

	public boolean setFloorPlan(String imagePath, String building_width, String building_height) {

		if (imagePath == null || imagePath.equals(""))
			return false;

		if (this.imagePath != null && this.imagePath.equals(imagePath))
			return true;

		Bitmap tempBitmap = BitmapFactory.decodeFile(imagePath);

		if (tempBitmap != null) {

			if (mBitmap != null)
				mBitmap.recycle();

			System.gc();

			this.imagePath = imagePath;
			this.mBitmap = tempBitmap;
			this.building_width = building_width;
			this.building_height = building_height;
			mZoomView.setImage(this.mBitmap);
			resetZoomState();
			resetLocation();
		} else
			return false;

		return true;
	}

	public boolean setLocationOnFloorPlan(String Geolocation) {

		if (Geolocation == null || mBitmap == null || building_width == null || building_height == null)
			return false;

		String coordinates[] = Geolocation.replace(", ", " ").split(" ");
		float x, y;
		float bitmapWidth;
		float bitmapHeight;

		try {
			x = Float.parseFloat(coordinates[0]);
			y = Float.parseFloat(coordinates[1]);
			bitmapWidth = Float.parseFloat(building_width);
			bitmapHeight = Float.parseFloat(building_height);
		} catch (Exception e) {
			return false;
		}

		// Clear all overlays if it is not tracking
		if (!trackMe.get()) {
			mZoomView.clearPoints();

			//add
			mZoomView.clearPath();
		}

		int x_pixels = (int) ((x * mBitmap.getWidth()) / bitmapWidth);
		int y_pixels = (int) ((y * mBitmap.getHeight()) / bitmapHeight);
		curClick.setClickPoint(x_pixels, y_pixels);
		curClick.notifyObservers();

		return true;
	}

	protected void onDestroy() {

		if (mBitmap != null)
			mBitmap.recycle();
		if (mZoomView != null)
			mZoomView.setOnTouchListener(null);
		if (mZoomControl != null)
			mZoomControl.getZoomState().deleteObservers();
	}

	/**
	 * Reset zoom state and notify observers
	 */
	private void resetZoomState() {
		mZoomControl.getZoomState().setPanX(0.5f);
		mZoomControl.getZoomState().setPanY(0.5f);
		mZoomControl.getZoomState().setZoom(1f);
		mZoomControl.getZoomState().notifyObservers();
		mZoomView.clearPoints();

		//add
		mZoomView.clearPath();
	}

	private void resetLocation() {
		curClick.setClickPoint(-1, -1);
		curClick.notifyObservers();
	}

	@Override
	public void update(Observable observable, Object data) {
		// Clear all overlays if it is not tracking
		if (!trackMe.get()) {
			mZoomView.clearPoints();
			//add
			mZoomView.clearPath();
		}

	}

	public boolean okBuildingSettings() {
		return this.mBitmap != null && this.building_height != null && this.building_width != null;
	}

}
