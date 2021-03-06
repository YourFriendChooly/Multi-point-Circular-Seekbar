/*
 *
 * Modifications Copyright 2017 Joseph Budic
 *
 * Thanks are due to the amazing work by Matt Joseph, hopefully the addition of multiple thumbs
 * will be helpful to some. This is a work in progress by an amateur android developer,
 * and as such may not be perfect. Feedback is graciously appreciated!
 *
 * -----------------------------------------------------------
 *
 * Unmodified code is Copyright 2013 Matt Joseph
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * 
 * This custom view/widget was inspired and guided by:
 * 
 * HoloCircleSeekBar - Copyright 2012 Jes�s Manzano
 * HoloColorPicker - Copyright 2012 Lars Werkman (Designed by Marie Schweiz)
 * 
 * Although I did not used the code from either project directly, they were both used as 
 * reference material, and as a result, were extremely helpful.
 *
 * -----------------------------------------------------------

 */

package com.wearelast.mpcs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.ListIterator;

public class CircularSeekBar extends View {

	/**
	 * Used to scale the dp units to pixels
	 */
	protected final float DPTOPX_SCALE = getResources().getDisplayMetrics().density;

	/**
	 * Minimum touch target size in DP. 48dp is the Android design recommendation
	 */
	protected final float MIN_TOUCH_TARGET_DP = 48;


	// Default values
	protected static final float DEFAULT_CIRCLE_X_RADIUS = 30f;
	protected static final float DEFAULT_CIRCLE_Y_RADIUS = 30f;
	protected static final float DEFAULT_POINTER_RADIUS = 7f;
	protected static final float DEFAULT_POINTER_HALO_WIDTH = 6f;
	protected static final float DEFAULT_POINTER_HALO_BORDER_WIDTH = 2f;
	protected static final float DEFAULT_CIRCLE_STROKE_WIDTH = 5f;
	protected static final float DEFAULT_START_ANGLE = 270f; // Geometric (clockwise, relative to 3 o'clock)
	protected static final float DEFAULT_END_ANGLE = 270f; // Geometric (clockwise, relative to 3 o'clock)
	protected static final int DEFAULT_MAX = 100;
	protected static final int DEFAULT_PROGRESS = 0;
	protected static final int DEFAULT_CIRCLE_COLOR = Color.DKGRAY;
	protected static final int DEFAULT_CIRCLE_PROGRESS_COLOR = Color.argb(235, 74, 138, 255);
	protected static final int DEFAULT_POINTER_COLOR = Color.argb(235, 74, 138, 255);
	protected static final int DEFAULT_POINTER_HALO_COLOR = Color.argb(135, 74, 138, 255);
	protected static final int DEFAULT_POINTER_HALO_COLOR_ONTOUCH = Color.argb(135, 74, 138, 255);
	protected static final int DEFAULT_CIRCLE_FILL_COLOR = Color.TRANSPARENT;
	protected static final int DEFAULT_POINTER_ALPHA = 135;
	protected static final int DEFAULT_POINTER_ALPHA_ONTOUCH = 100;
	protected static final boolean DEFAULT_USE_CUSTOM_RADII = false;
	protected static final boolean DEFAULT_MAINTAIN_EQUAL_CIRCLE = true;
	protected static final boolean DEFAULT_MOVE_OUTSIDE_CIRCLE = false;
	protected static final boolean DEFAULT_LOCK_ENABLED = true;

	/**
	 * {@code Paint} instance used to draw the inactive circle.
	 */
	protected Paint mCirclePaint;

	/**
	 * {@code Paint} instance used to draw the circle fill.
	 */
	protected Paint mCircleFillPaint;

	/**
	 * {@code Paint} instance used to draw the active circle (represents progress).
	 */
	protected Paint mCircleProgressPaint;

	/**
	 * {@code Paint} instance used to draw the glow from the active circle.
	 */
	protected Paint mCircleProgressGlowPaint;

	/**
	 * {@code Paint} instance used to draw the center of the pointer.
	 * Note: This is broken on 4.0+, as BlurMasks do not work with hardware acceleration.
	 */
	protected Paint mPointerPaint;

	/**
	 * {@code Paint} instance used to draw the halo of the pointer.
	 * Note: The halo is the part that changes transparency.
	 */
	protected Paint mPointerHaloPaint;

	/**
	 * {@code Paint} instance used to draw the border of the pointer, outside of the halo.
	 */
	protected Paint mPointerHaloBorderPaint;

	/**
	 * The width of the circle (in pixels).
	 */
	protected float mCircleStrokeWidth;

	/**
	 * The X radius of the circle (in pixels).
	 */
	protected float mCircleXRadius;

	/**
	 * The Y radius of the circle (in pixels).
	 */
	protected float mCircleYRadius;

	/**
	 * The radius of the pointer (in pixels).
	 */
	protected float mPointerRadius;

	/**
	 * The width of the pointer halo (in pixels).
	 */
	protected float mPointerHaloWidth;

	/**
	 * The width of the pointer halo border (in pixels).
	 */
	protected float mPointerHaloBorderWidth;

	/**
	 * Start angle of the CircularSeekBar.
	 * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
	 * from the mEndAngle to make the circle function properly.
	 */
	protected float mStartAngle;

	/**
	 * End angle of the CircularSeekBar.
	 * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
	 * from the mEndAngle to make the circle function properly.
	 */
	protected float mEndAngle;

	/**
	 * {@code RectF} that represents the circle (or ellipse) of the seekbar.
	 */
	protected RectF mCircleRectF = new RectF();

	/**
	 * Holds the color value for {@code mPointerPaint} before the {@code Paint} instance is created.
	 */
	protected int mPointerColor = DEFAULT_POINTER_COLOR;

	/**
	 * Holds the color value for {@code mPointerHaloPaint} before the {@code Paint} instance is created.
	 */
	protected int mPointerHaloColor = DEFAULT_POINTER_HALO_COLOR;

	/**
	 * Holds the color value for {@code mPointerHaloPaint} before the {@code Paint} instance is created.
	 */
	protected int mPointerHaloColorOnTouch = DEFAULT_POINTER_HALO_COLOR_ONTOUCH;

	/**
	 * Holds the color value for {@code mCirclePaint} before the {@code Paint} instance is created.
	 */
	protected int mCircleColor = DEFAULT_CIRCLE_COLOR;

	/**
	 * Holds the color value for {@code mCircleFillPaint} before the {@code Paint} instance is created.
	 */
	protected int mCircleFillColor = DEFAULT_CIRCLE_FILL_COLOR;

	/**
	 * Holds the color value for {@code mCircleProgressPaint} before the {@code Paint} instance is created.
	 */
	protected int mCircleProgressColor = DEFAULT_CIRCLE_PROGRESS_COLOR;

	/**
	 * Holds the alpha value for {@code mPointerHaloPaint}.
	 */
	protected int mPointerAlpha = DEFAULT_POINTER_ALPHA;

	/**
	 * Holds the OnTouch alpha value for {@code mPointerHaloPaint}.
	 */
	protected int mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ONTOUCH;

	/**
	 * Distance (in degrees) that the the circle/semi-circle makes up.
	 * This amount represents the max of the circle in degrees.
	 */
	protected float mTotalCircleDegrees;

	/**
	 * {@code Path} used to draw the circle/semi-circle.
	 */
	protected Path mCirclePath;

	/**
	 * Max value that this CircularSeekBar is representing.
	 */
	protected int mMax;

	/**
	 * Progress value that this CircularSeekBar is representing.
	 */
	protected int mProgress;

	/**
	 * If true, then the user can specify the X and Y radii.
	 * If false, then the View itself determines the size of the CircularSeekBar.
	 */
	protected boolean mCustomRadii;

	/**
	 * Maintain a perfect circle (equal x and y radius), regardless of view or custom attributes.
	 * The smaller of the two radii will always be used in this case.
	 * The default is to be a circle and not an ellipse, due to the behavior of the ellipse.
	 */
	protected boolean mMaintainEqualCircle;

	/**
	 * Once a user has touched the circle, this determines if moving outside the circle is able
	 * to change the position of the pointer (and in turn, the progress).
	 */
	protected boolean mMoveOutsideCircle;

	/**
	 * Used for enabling/disabling the lock option for easier hitting of the 0 progress mark.
	 */
	protected boolean lockEnabled = true;

	/**
	 * Used for when the user moves beyond the start of the circle when moving counter clockwise.
	 * Makes it easier to hit the 0 progress mark.
	 */
	protected boolean lockAtStart = true;

	/**
	 * Used for when the user moves beyond the end of the circle when moving clockwise.
	 * Makes it easier to hit the 100% (max) progress mark.
	 */
	protected boolean lockAtEnd = false;

	/**
	 * When the user is touching the circle on ACTION_DOWN, this is set to true.
	 * Used when touching the CircularSeekBar.
	 */
	protected boolean mUserIsMovingPointer = false;

	/**
	 * Represents the clockwise distance from {@code mStartAngle} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 */
	protected float cwDistanceFromStart;

	/**
	 * Represents the counter-clockwise distance from {@code mStartAngle} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 */
	protected float ccwDistanceFromStart;

	/**
	 * Represents the clockwise distance from {@code mEndAngle} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 */
	protected float cwDistanceFromEnd;

	/**
	 * Represents the counter-clockwise distance from {@code mEndAngle} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 * Currently unused, but kept just in case.
	 */
	@SuppressWarnings("unused")
	protected float ccwDistanceFromEnd;

	/**
	 * The previous touch action value for {@code cwDistanceFromStart}.
	 * Used when touching the CircularSeekBar.
	 */
	protected float lastCWDistanceFromStart;

	/**
	 * Represents the clockwise distance from {@code mPointerPosition} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 */
	protected float cwDistanceFromPointer;

	/**
	 * Represents the counter-clockwise distance from {@code mPointerPosition} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 */
	protected float ccwDistanceFromPointer;

	/**
	 * True if the user is moving clockwise around the circle, false if moving counter-clockwise.
	 * Used when touching the CircularSeekBar.
	 */
	protected boolean mIsMovingCW;

	/**
	 * The width of the circle used in the {@code RectF} that is used to draw it.
	 * Based on either the View width or the custom X radius.
	 */
	protected float mCircleWidth;

	/**
	 * The height of the circle used in the {@code RectF} that is used to draw it.
	 * Based on either the View width or the custom Y radius.
	 */
	protected float mCircleHeight;

	/**
	 * Listener.
	 */
	protected OnCircularSeekBarChangeListener mOnCircularSeekBarChangeListener;

	/**
	 * True if user touch input is enabled, false if user touch input is ignored.
	 * This does not affect setting values programmatically.
	 */
	protected boolean isTouchEnabled = true;

	/**
	 * Initialize the CircularSeekBar with the attributes from the XML style.
	 * Uses the defaults defined at the top of this file when an attribute is not specified by the user.
	 * @param attrArray TypedArray containing the attributes.
	 */
	protected void initAttributes(TypedArray attrArray) {
		mCircleXRadius = attrArray.getDimension(R.styleable.CircularSeekBar_circle_x_radius, DEFAULT_CIRCLE_X_RADIUS * DPTOPX_SCALE);
		mCircleYRadius = attrArray.getDimension(R.styleable.CircularSeekBar_circle_y_radius, DEFAULT_CIRCLE_Y_RADIUS * DPTOPX_SCALE);
		mPointerRadius = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_radius, DEFAULT_POINTER_RADIUS * DPTOPX_SCALE);
		mPointerHaloWidth = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_halo_width, DEFAULT_POINTER_HALO_WIDTH * DPTOPX_SCALE);
		mPointerHaloBorderWidth = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_halo_border_width, DEFAULT_POINTER_HALO_BORDER_WIDTH * DPTOPX_SCALE);
		mCircleStrokeWidth = attrArray.getDimension(R.styleable.CircularSeekBar_circle_stroke_width, DEFAULT_CIRCLE_STROKE_WIDTH * DPTOPX_SCALE);

		mPointerColor = attrArray.getColor(R.styleable.CircularSeekBar_pointer_color, DEFAULT_POINTER_COLOR);
		mPointerHaloColor = attrArray.getColor(R.styleable.CircularSeekBar_pointer_halo_color, DEFAULT_POINTER_HALO_COLOR);
		mPointerHaloColorOnTouch = attrArray.getColor(R.styleable.CircularSeekBar_pointer_halo_color_ontouch, DEFAULT_POINTER_HALO_COLOR_ONTOUCH);
		mCircleColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_color, DEFAULT_CIRCLE_COLOR);
		mCircleProgressColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_progress_color, DEFAULT_CIRCLE_PROGRESS_COLOR);
		mCircleFillColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_fill, DEFAULT_CIRCLE_FILL_COLOR);

		mPointerAlpha = Color.alpha(mPointerHaloColor);

		mPointerAlphaOnTouch = attrArray.getInt(R.styleable.CircularSeekBar_pointer_alpha_ontouch, DEFAULT_POINTER_ALPHA_ONTOUCH);
		if (mPointerAlphaOnTouch > 255 || mPointerAlphaOnTouch < 0) {
			mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ONTOUCH;
		}

		mMax = attrArray.getInt(R.styleable.CircularSeekBar_max, DEFAULT_MAX);
		mProgress = attrArray.getInt(R.styleable.CircularSeekBar_progress, DEFAULT_PROGRESS);
		mCustomRadii = attrArray.getBoolean(R.styleable.CircularSeekBar_use_custom_radii, DEFAULT_USE_CUSTOM_RADII);
		mMaintainEqualCircle = attrArray.getBoolean(R.styleable.CircularSeekBar_maintain_equal_circle, DEFAULT_MAINTAIN_EQUAL_CIRCLE);
		mMoveOutsideCircle = attrArray.getBoolean(R.styleable.CircularSeekBar_move_outside_circle, DEFAULT_MOVE_OUTSIDE_CIRCLE);
		lockEnabled = attrArray.getBoolean(R.styleable.CircularSeekBar_lock_enabled, DEFAULT_LOCK_ENABLED);

		// Modulo 360 right now to avoid constant conversion
		mStartAngle = ((360f + (attrArray.getFloat((R.styleable.CircularSeekBar_start_angle), DEFAULT_START_ANGLE) % 360f)) % 360f);
		mEndAngle = ((360f + (attrArray.getFloat((R.styleable.CircularSeekBar_end_angle), DEFAULT_END_ANGLE) % 360f)) % 360f);

		if (mStartAngle == mEndAngle) {
			//mStartAngle = mStartAngle + 1f;
			mEndAngle = mEndAngle - .1f;
		}
	}

	/**
	 * Initializes the {@code Paint} objects with the appropriate styles.
	 */
	protected void initPaints() {
		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setDither(true);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
		mCirclePaint.setStrokeCap(Paint.Cap.ROUND);

		mCircleFillPaint = new Paint();
		mCircleFillPaint.setAntiAlias(true);
		mCircleFillPaint.setDither(true);
		mCircleFillPaint.setColor(mCircleFillColor);
		mCircleFillPaint.setStyle(Paint.Style.FILL);

		mCircleProgressPaint = new Paint();
		mCircleProgressPaint.setAntiAlias(true);
		mCircleProgressPaint.setDither(true);
		mCircleProgressPaint.setColor(mCircleProgressColor);
		mCircleProgressPaint.setStrokeWidth(mCircleStrokeWidth);
		mCircleProgressPaint.setStyle(Paint.Style.STROKE);
		mCircleProgressPaint.setStrokeJoin(Paint.Join.ROUND);
		mCircleProgressPaint.setStrokeCap(Paint.Cap.ROUND);

		mCircleProgressGlowPaint = new Paint();
		mCircleProgressGlowPaint.set(mCircleProgressPaint);
		mCircleProgressGlowPaint.setMaskFilter(new BlurMaskFilter((5f * DPTOPX_SCALE), BlurMaskFilter.Blur.NORMAL));

		mPointerPaint = new Paint();
		mPointerPaint.setAntiAlias(true);
		mPointerPaint.setDither(true);
		mPointerPaint.setStyle(Paint.Style.FILL);
		mPointerPaint.setColor(mPointerColor);
		mPointerPaint.setStrokeWidth(mPointerRadius);

		mPointerHaloPaint = new Paint();
		mPointerHaloPaint.set(mPointerPaint);
		mPointerHaloPaint.setColor(mPointerHaloColor);
		mPointerHaloPaint.setAlpha(mPointerAlpha);
		mPointerHaloPaint.setStrokeWidth(mPointerRadius + mPointerHaloWidth);

		mPointerHaloBorderPaint = new Paint();
		mPointerHaloBorderPaint.set(mPointerPaint);
		mPointerHaloBorderPaint.setStrokeWidth(mPointerHaloBorderWidth);
		mPointerHaloBorderPaint.setStyle(Paint.Style.STROKE);

	}

	/**
	 * Calculates the total degrees between mStartAngle and mEndAngle, and sets mTotalCircleDegrees
	 * to this value.
	 */
	protected void calculateTotalDegrees() {
		mTotalCircleDegrees = (360f - (mStartAngle - mEndAngle)) % 360f; // Length of the entire circle/arc
		if (mTotalCircleDegrees <= 0f) {
			mTotalCircleDegrees = 360f;
		}
	}

	/**
	 * Initialize the {@code Path} objects with the appropriate values.
	 */
	protected void initPaths() {
		mCirclePath = new Path();
		mCirclePath.addArc(mCircleRectF, mStartAngle, mTotalCircleDegrees);

		initAllPointers();

	}

	/**
	 * Initialize the {@code RectF} objects with the appropriate values.
	 */
	protected void initRects() {
		mCircleRectF.set(-mCircleWidth, -mCircleHeight, mCircleWidth, mCircleHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.translate(this.getWidth() / 2, this.getHeight() / 2);

		canvas.drawPath(mCirclePath, mCirclePaint);
		drawPointerProgress(canvas);

		canvas.drawPath(mCirclePath, mCircleFillPaint);

		drawPointers(canvas);

	}


	protected void recalculateAll() {
		calculateTotalDegrees();
		calculateAllPointerAngle();
		calculateAllProgressDegrees();

		initRects();

		initPaths();

		calculateAllPositions();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		if (mMaintainEqualCircle) {
			int min = Math.min(width, height);
			setMeasuredDimension(min, min);
		} else {
			setMeasuredDimension(width, height);
		}

		// Set the circle width and height based on the view for the moment
		mCircleHeight = (float)height / 2f - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
		mCircleWidth = (float)width / 2f - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);

		// If it is not set to use custom
		if (mCustomRadii) {
			// Check to make sure the custom radii are not out of the view. If they are, just use the view values
			if ((mCircleYRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth) < mCircleHeight) {
				mCircleHeight = mCircleYRadius - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
			}

			if ((mCircleXRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth) < mCircleWidth) {
				mCircleWidth = mCircleXRadius - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
			}
		}

		if (mMaintainEqualCircle) { // Applies regardless of how the values were determined
			float min = Math.min(mCircleHeight, mCircleWidth);
			mCircleHeight = min;
			mCircleWidth = min;
		}

		recalculateAll();
	}

	/**
	 * Get whether the pointer locks at zero and max.
	 * @return Boolean value of true if the pointer locks at zero and max, false if it does not.
	 */
	public boolean isLockEnabled() {
		return lockEnabled;
	}

	/**
	 * Set whether the pointer locks at zero and max or not.
	 * @param boolean value. True if the pointer should lock at zero and max, false if it should not.
	 */
	public void setLockEnabled(boolean lockEnabled) {
		this.lockEnabled = lockEnabled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!isTouchEnabled){
			return false;
		}

		// Convert coordinates to our internal coordinate system
		float x = event.getX() - getWidth() / 2;
		float y = event.getY() - getHeight() / 2;

		// Get the distance from the center of the circle in terms of x and y
		float distanceX = mCircleRectF.centerX() - x;
		float distanceY = mCircleRectF.centerY() - y;

		// Get the distance from the center of the circle in terms of a radius
		float touchEventRadius = (float) Math.sqrt((Math.pow(distanceX, 2) + Math.pow(distanceY, 2)));

		float minimumTouchTarget = MIN_TOUCH_TARGET_DP * DPTOPX_SCALE; // Convert minimum touch target into px
		float additionalRadius; // Either uses the minimumTouchTarget size or larger if the ring/pointer is larger

		if (mCircleStrokeWidth < minimumTouchTarget) { // If the width is less than the minimumTouchTarget, use the minimumTouchTarget
			additionalRadius = minimumTouchTarget / 2;
		}
		else {
			additionalRadius = mCircleStrokeWidth / 2; // Otherwise use the width
		}
		float outerRadius = Math.max(mCircleHeight, mCircleWidth) + additionalRadius; // Max outer radius of the circle, including the minimumTouchTarget or wheel width
		float innerRadius = Math.min(mCircleHeight, mCircleWidth) - additionalRadius; // Min inner radius of the circle, including the minimumTouchTarget or wheel width

		if (mPointerRadius < (minimumTouchTarget / 2)) { // If the pointer radius is less than the minimumTouchTarget, use the minimumTouchTarget
			additionalRadius = minimumTouchTarget / 2;
		}
		else {
			additionalRadius = mPointerRadius; // Otherwise use the radius
		}

		float touchAngle;
		touchAngle = (float) ((java.lang.Math.atan2(y, x) / Math.PI * 180) % 360); // Verified
		touchAngle = (touchAngle < 0 ? 360 + touchAngle : touchAngle); // Verified

		cwDistanceFromStart = touchAngle - mStartAngle; // Verified
		cwDistanceFromStart = (cwDistanceFromStart < 0 ? 360f + cwDistanceFromStart : cwDistanceFromStart); // Verified
		ccwDistanceFromStart = 360f - cwDistanceFromStart; // Verified

		cwDistanceFromEnd = touchAngle - mEndAngle; // Verified
		cwDistanceFromEnd = (cwDistanceFromEnd < 0 ? 360f + cwDistanceFromEnd : cwDistanceFromEnd); // Verified
		ccwDistanceFromEnd = 360f - cwDistanceFromEnd; // Verified

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// These are only used for ACTION_DOWN for handling if the pointer was the part that was touched
			float pointerRadiusDegrees = (float) ((mPointerRadius * 180) / (Math.PI * Math.max(mCircleHeight, mCircleWidth)));
			try {
				touchedPointer = calculatePointerTouched(touchAngle, touchEventRadius, innerRadius, outerRadius, pointerRadiusDegrees);
				lastCWDistanceFromStart = cwDistanceFromStart;
				touchedPointer.mPointerHaloPaint.setAlpha(mPointerAlphaOnTouch);
				touchedPointer.mPointerHaloPaint.setColor(mPointerHaloColorOnTouch);
				recalculateAll();
				invalidate();
				if (mOnCircularSeekBarChangeListener != null) {
					mOnCircularSeekBarChangeListener.onStartTrackingTouch(this, pPointerList.get(pPointerList.indexOf(touchedPointer)));
				}
				mUserIsMovingPointer = true;
				lockAtEnd = false;
				lockAtStart = false;
			} catch (NullPointerException e){
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				if (lastCWDistanceFromStart < cwDistanceFromStart) {
					if ((cwDistanceFromStart - lastCWDistanceFromStart) > 180f && !mIsMovingCW) {
						lockAtStart = true;
						lockAtEnd = false;
					} else {
						mIsMovingCW = true;
					}
				} else {
					if ((lastCWDistanceFromStart - cwDistanceFromStart) > 180f && mIsMovingCW) {
						lockAtEnd = true;
						lockAtStart = false;
					} else {
						mIsMovingCW = false;
					}
				}

				if (lockAtStart && mIsMovingCW) {
					lockAtStart = false;
				}
				if (lockAtEnd && !mIsMovingCW) {
					lockAtEnd = false;
				}
				if (lockAtStart && !mIsMovingCW && (ccwDistanceFromStart > 90)) {
					lockAtStart = false;
				}
				if (lockAtEnd && mIsMovingCW && (cwDistanceFromEnd > 90)) {
					lockAtEnd = false;
				}
				// Fix for passing the end of a semi-circle quickly
				if (!lockAtEnd && cwDistanceFromStart > mTotalCircleDegrees && mIsMovingCW && lastCWDistanceFromStart < mTotalCircleDegrees) {
					lockAtEnd = true;
				}

				if (lockAtStart && lockEnabled) {
					// TODO: Add a check if mProgress is already 0, in which case don't call the listener
					mProgress = 0;
					recalculateAll();
					invalidate();
					if (touchedPointer.changeListener != null) {
                        touchedPointer.changeListener.onProgressChanged(this, touchedPointer.mProgress, getRelativeProgress(touchedPointer), pPointerList.get(pPointerList.indexOf(touchedPointer)), true);
					}

				} else if (lockAtEnd && lockEnabled) {
					mProgress = mMax;
					recalculateAll();
					invalidate();
					if (mOnCircularSeekBarChangeListener != null) {
						touchedPointer.changeListener.onProgressChanged(this, touchedPointer.mProgress, getRelativeProgress(touchedPointer), pPointerList.get(pPointerList.indexOf(touchedPointer)), true);
					}
				} else if ((mMoveOutsideCircle) || (touchEventRadius <= outerRadius)) {
					if (!(cwDistanceFromStart > mTotalCircleDegrees)) {
						if (mUserIsMovingPointer) {
                            try {
								int priorProgress = touchedPointer.getProgress();
                                touchedPointer.setProgressBasedOnAngle(touchAngle);
								setGangProgress(touchedPointer, priorProgress);
                            } catch (Exception e) {
                            }
                        } else {
                        }

					}
					recalculateAll();
					invalidate();
					if (touchedPointer.changeListener != null) {
                        touchedPointer.changeListener.onProgressChanged(this, touchedPointer.mProgress, getRelativeProgress(touchedPointer), pPointerList.get(pPointerList.indexOf(touchedPointer)), true);
					}
				} else {
					break;
				}

				lastCWDistanceFromStart = cwDistanceFromStart;
			} else {
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			try {
			}catch (NullPointerException e){

			}

			if (mUserIsMovingPointer) {
				mUserIsMovingPointer = false;
				invalidate();
				if (touchedPointer.changeListener != null) {
                    touchedPointer.changeListener.onStopTrackingTouch(this, pPointerList.get(pPointerList.indexOf(touchedPointer)));
				}
			} else {
				return false;
			}
			break;
		case MotionEvent.ACTION_CANCEL: // Used when the parent view intercepts touches for things like scrolling
			try {
			}catch (NullPointerException e){

			}
			mUserIsMovingPointer = false;
			invalidate();
			break;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}

		return true;
	}

	protected void setGangProgress(Pointer touched, int priorProgress){
		int currentProgress = touched.getProgress();
		int progressDifference = currentProgress - priorProgress;
		ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
		while (pPointerIterator.hasNext()) {
			Pointer p = pPointerIterator.next();
			int accessedProgress = p.getProgress();
			if (accessedProgress > currentProgress){
				p.setProgress(p.getProgress()+progressDifference);
			}
		}
	}

	protected void init(AttributeSet attrs, int defStyle) {
		final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircularSeekBar, defStyle, 0);

		initAttributes(attrArray);

		attrArray.recycle();

		initPaints();
	}

	public CircularSeekBar(Context context) {
		super(context);
		init(null, 0);
	}

	public CircularSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable("PARENT", superState);
		state.putInt("MAX", mMax);
		state.putInt("PROGRESS", mProgress);
		state.putInt("mCircleColor", mCircleColor);
		state.putInt("mCircleProgressColor", mCircleProgressColor);
		state.putInt("mPointerColor", mPointerColor);
		state.putInt("mPointerHaloColor", mPointerHaloColor);
		state.putInt("mPointerHaloColorOnTouch", mPointerHaloColorOnTouch);
		state.putInt("mPointerAlpha", mPointerAlpha);
		state.putInt("mPointerAlphaOnTouch", mPointerAlphaOnTouch);
		state.putBoolean("lockEnabled", lockEnabled);
		state.putBoolean("isTouchEnabled", isTouchEnabled);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable("PARENT");
		super.onRestoreInstanceState(superState);

		mMax = savedState.getInt("MAX");
		mProgress = savedState.getInt("PROGRESS");
		mCircleColor = savedState.getInt("mCircleColor");
		mCircleProgressColor = savedState.getInt("mCircleProgressColor");
		mPointerColor = savedState.getInt("mPointerColor");
		mPointerHaloColor = savedState.getInt("mPointerHaloColor");
		mPointerHaloColorOnTouch = savedState.getInt("mPointerHaloColorOnTouch");
		mPointerAlpha = savedState.getInt("mPointerAlpha");
		mPointerAlphaOnTouch = savedState.getInt("mPointerAlphaOnTouch");
		lockEnabled = savedState.getBoolean("lockEnabled");
		isTouchEnabled = savedState.getBoolean("isTouchEnabled");

		initPaints();

		recalculateAll();
	}

	public void setOnSeekBarChangeListener(OnCircularSeekBarChangeListener l) {
		mOnCircularSeekBarChangeListener = l;
	}

	/**
	* Listener for the CircularSeekBar. Implements the same methods as the normal OnSeekBarChangeListener.
	*/
	public interface OnCircularSeekBarChangeListener {

		public abstract void onProgressChanged(CircularSeekBar circularSeekBar, int absoloutePprogress, float relativeProgress, Pointer pointer, boolean fromUser);

		public abstract void onStopTrackingTouch(CircularSeekBar circularSeekBar, Pointer pointer);

		public abstract void onStartTrackingTouch(CircularSeekBar seekBar, Pointer pointer);
	}

	/**
	 * Sets the circle color.
	 * @param color the color of the circle
	 */
	public void setCircleColor(int color) {
		mCircleColor = color;
		mCirclePaint.setColor(mCircleColor);
		invalidate();
	}

	/**
	 * Gets the circle color.
	 * @return An integer color value for the circle
	 */
	public int getCircleColor() {
		return mCircleColor;
	}

	/**
	 * Sets the circle progress color.
	 * @param color the color of the circle progress
	 */
	public void setCircleProgressColor(int color) {
		mCircleProgressColor = color;
		mCircleProgressPaint.setColor(mCircleProgressColor);
		invalidate();
	}

	/**
	 * Gets the circle progress color.
	 * @return An integer color value for the circle progress
	 */
	public int getCircleProgressColor() {
		return mCircleProgressColor;
	}

	/**
	 * Sets the pointer color.
	 * @param color the color of the pointer
	 */
	public void setPointerColor(int color) {
		mPointerColor = color;
		mPointerPaint.setColor(mPointerColor);
		invalidate();
	}

	/**
	 * Gets the pointer color.
	 * @return An integer color value for the pointer
	 */
	public int getPointerColor() {
		return mPointerColor;
	}

	/**
	 * Sets the pointer halo color.
	 * @param color the color of the pointer halo
	 */
	public void setPointerHaloColor(int color) {
		mPointerHaloColor = color;
		mPointerHaloPaint.setColor(mPointerHaloColor);
		invalidate();
	}

	/**
	 * Gets the pointer halo color.
	 * @return An integer color value for the pointer halo
	 */
	public int getPointerHaloColor() {
		return mPointerHaloColor;
	}

	/**
	 * Sets the pointer alpha.
	 * @param alpha the alpha of the pointer
	 */
	public void setPointerAlpha(int alpha) {
		if (alpha >=0 && alpha <= 255) {
			mPointerAlpha = alpha;
			mPointerHaloPaint.setAlpha(mPointerAlpha);
			invalidate();
		}
	}

	/**
	 * Gets the pointer alpha value.
	 * @return An integer alpha value for the pointer (0..255)
	 */
	public int getPointerAlpha() {
		return mPointerAlpha;
	}

	/**
	 * Sets the pointer alpha when touched.
	 * @param alpha the alpha of the pointer (0..255) when touched
	 */
	public void setPointerAlphaOnTouch(int alpha) {
		if (alpha >=0 && alpha <= 255) {
			mPointerAlphaOnTouch = alpha;
		}
	}

	/**
	 * Gets the pointer alpha value when touched.
	 * @return An integer alpha value for the pointer (0..255) when touched
	 */
	public int getPointerAlphaOnTouch() {
		return mPointerAlphaOnTouch;
	}

	/**
	 * Sets the circle fill color.
	 * @param color the color of the circle fill
	 */
	public void setCircleFillColor(int color) {
		mCircleFillColor = color;
		mCircleFillPaint.setColor(mCircleFillColor);
		invalidate();
	}

	/**
	 * Gets the circle fill color.
	 * @return An integer color value for the circle fill
	 */
	public int getCircleFillColor() {
		return mCircleFillColor;
	}

	/**
	 * Set the max of the CircularSeekBar.
	 * If the new max is less than the current progress, then the progress will be set to zero.
	 * If the progress is changed as a result, then any listener will receive a onProgressChanged event.
	 * @param max The new max for the CircularSeekBar.
	 */
	public void setMax(int max) {
		if (!(max <= 0)) { // Check to make sure it's greater than zero
			if (max <= mProgress) {
				mProgress = 0; // If the new max is less than current progress, set progress to zero
				if (mOnCircularSeekBarChangeListener != null) {
					mOnCircularSeekBarChangeListener.onProgressChanged(this, mProgress, getRelativeProgress(touchedPointer), false);
				}
			}
			mMax = max;

			recalculateAll();
			invalidate();
		}
	}

	/**
	 * Get the current max of the CircularSeekBar.
	 * @return Synchronized integer value of the max.
	 */
	public synchronized int getMax() {
		return mMax;
	}

	/**
	 * Set whether user touch input is accepted or ignored.
	 * param boolean value. True if user touch input is to be accepted, false if user touch input is to be ignored.
	 */
	public void setIsTouchEnabled(boolean isTouchEnabled) {
		this.isTouchEnabled = isTouchEnabled;
	}

	/**
	 * Get whether user touch input is accepted.
	 * @return Boolean value of true if user touch input is accepted, false if user touch input is ignored.
	 */
	public boolean getIsTouchEnabled() {
		return isTouchEnabled;
	}


	/**
	 * Instantiatable Pointers addition contributed by Joseph Novak.
	 */
	protected ArrayList<Pointer> pPointerList = new ArrayList<>();
    protected Pointer touchedPointer;

	public Pointer addPointer(int pProgress, OnCircularSeekBarChangeListener l){
		Pointer pointer = new Pointer(pProgress, this, l);
		pPointerList.add(pointer);
		return pointer;
	}

	public Pointer addPointer(int pProgress){
		Pointer pointer = new Pointer(pProgress, this);
		pPointerList.add(pointer);
		return pointer;
	}

	public Pointer addPointer(){
		Pointer pointer = new Pointer(this);
		pPointerList.add(pointer);
		distributeProgress();
        return pointer;
	};

    public Pointer addPointer(OnCircularSeekBarChangeListener l){
        Pointer pointer = new Pointer(this, l);
        pPointerList.add(pointer);
        distributeProgress();
        return pointer;
    };

    //distributeProgress will position the pointers evenly around the maximum value of the circular seekbar.
    protected void distributeProgress(){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
            Pointer p = pPointerIterator.next();
            int distributiveProgess = this.getMax() / (pPointerList.size()) * pPointerList.indexOf(p);
            p.setProgress(distributiveProgess);
        }
    }

	protected void drawPointerProgress(Canvas c){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
			Pointer p = pPointerIterator.next();
			try {
				c.drawPath(p.mCircleProgressPath, p.mCircleProgressGlowPaint);
				c.drawPath(p.mCircleProgressPath, p.mCircleProgressPaint);
			} catch (NullPointerException e){
			}

		}
	}

	protected void drawPointers(Canvas c){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
			Pointer p = pPointerIterator.next();
			c.drawCircle(p.mPointerPositionXY[0], p.mPointerPositionXY[1], mPointerRadius + mPointerHaloWidth, p.mPointerHaloPaint);
			c.drawCircle(p.mPointerPositionXY[0], p.mPointerPositionXY[1], mPointerRadius, p.mPointerPaint);
			if (mUserIsMovingPointer) {
				c.drawCircle(p.mPointerPositionXY[0], p.mPointerPositionXY[1], mPointerRadius + mPointerHaloWidth + (mPointerHaloBorderWidth / 2f), p.mPointerHaloBorderPaint);
			}
		}
	}

	protected void calculateAllPositions(){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
			Pointer p = pPointerIterator.next();
			p.calculatePointerXYPosition();
		}

	}

	protected void initAllPointers(){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
			Pointer p = pPointerIterator.next();
			p.init();
		}
	}

    protected void calculateAllPointerAngle(){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
            Pointer p = pPointerIterator.next();
            p.calculatePointerAngle();
        }
    }

    protected void calculateAllProgressDegrees(){
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        while (pPointerIterator.hasNext()){
            Pointer p = pPointerIterator.next();
            p.calculateProgressDegrees();
        }
    }

    protected Pointer calculatePointerTouched(float touchAngle, float touchEventRadius, float innerRadius, float outerRadius, float pointerRadiusDegrees) {
		ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
		Pointer p = null;
		while (pPointerIterator.hasNext()) {
			p = pPointerIterator.next();
			cwDistanceFromPointer = touchAngle - p.mPointerPosition;
			cwDistanceFromPointer = (cwDistanceFromPointer < 0 ? 360f + cwDistanceFromPointer : cwDistanceFromPointer);
			ccwDistanceFromPointer = 360f - cwDistanceFromPointer;
			// This is for if the first touch is on the actual pointer.
			if (((touchEventRadius >= innerRadius + 3) && (touchEventRadius <= outerRadius + 3)) && ((cwDistanceFromPointer <= pointerRadiusDegrees + 3) || (ccwDistanceFromPointer <= pointerRadiusDegrees + 3))) {
				//p.setProgressBasedOnAngle(p.mPointerPosition);
				mIsMovingCW = true;
				touchedPointer = p;
				return touchedPointer;
			} else if (cwDistanceFromStart > mTotalCircleDegrees) { // If the user is touching outside of the start AND end
				mUserIsMovingPointer = false;
				continue;
			} else if ((touchEventRadius >= innerRadius) && (touchEventRadius <= outerRadius)) { // If the user is touching near the circle
				//TODO get a toggle on this for instant touch.
				//p.setProgressBasedOnAngle(touchAngle);
				p = null;
				continue;

			} else { // If the user is not touching near the circle
				mUserIsMovingPointer = false;
				return null;
			}
		}

		return p;

	}

    public int getRelativeProgress(Pointer currentPointer) {
        ListIterator<Pointer> pPointerIterator = pPointerList.listIterator();
        int nearestNeighbor = 0;
        int touched = currentPointer.getProgress();
        while (pPointerIterator.hasNext()) {
            Pointer p = pPointerIterator.next();
            int accessed = p.getProgress();
            if (touched > accessed && (accessed > nearestNeighbor && accessed < touched)){
                nearestNeighbor = accessed;
            }
        }
        int relativeProgress = touched-nearestNeighbor;
        return relativeProgress;
    }

	protected class Pointer{
		protected float[] mPointerPositionXY = new float[2];
		CircularSeekBar seek;
		Path mCircleProgressPath;
		int mProgress;
        float mPointerPosition;
        float mProgressDegrees;
        OnCircularSeekBarChangeListener changeListener;
		Paint mPointerHaloPaint;
		Paint mPointerPaint;
		Paint mPointerHaloBorderPaint;
		Paint mCircleProgressPaint;
		Paint mCircleProgressGlowPaint;

		protected boolean CUSTOM_COLOR = false;
		int pointerColor;
		int progressColor;

        public void setPointerColor(int color) {
			this.CUSTOM_COLOR = true;
			this.pointerColor = color;
		};

		public void setProgressColor(int color) {
			this.CUSTOM_COLOR = true;
			this.progressColor = color;
		};

		public void setColor(int color){
			this.CUSTOM_COLOR = true;
			this.pointerColor = color;
			this.progressColor = color;
		}

		//Constructor with a defined progress position (in percentage of total), as well as a change listener
        public Pointer(int pProgress, CircularSeekBar circularSeekBar, OnCircularSeekBarChangeListener l){
			seek = circularSeekBar;
			setProgress(pProgress);
            this.changeListener = l;
		}

        //Constructor with distributed thumbs and a ChangeListener
        public Pointer(CircularSeekBar circularSeekBar, OnCircularSeekBarChangeListener l){
            seek = circularSeekBar;
            this.changeListener = l;
        }

        //Constructer without a change listener
		public Pointer(int pProgress, CircularSeekBar circularSeekBar){
			seek = circularSeekBar;
			setProgress(pProgress);
		}

        //No value constructor will distribute the pointer positions evenly around the Max value of the circular seekbar.
		public Pointer(CircularSeekBar circularSeekBar){
			seek = circularSeekBar;
		}

		private void setProgress(int progress) {
			if (this.mProgress != progress) {
				this.mProgress = progress;
				if (mOnCircularSeekBarChangeListener != null) {
					mOnCircularSeekBarChangeListener.onProgressChanged(seek, progress, getRelativeProgress(touchedPointer), false);
				}

				recalculateAll();
				invalidate();
			}
		}

		private void calculatePointerXYPosition() {
			PathMeasure pm = new PathMeasure(this.mCircleProgressPath, false);
			boolean returnValue = pm.getPosTan(pm.getLength(), this.mPointerPositionXY, null);
			if (!returnValue) {
				pm = new PathMeasure(mCirclePath, false);
				returnValue = pm.getPosTan(0, this.mPointerPositionXY, null);
			}
		}

		private void init(){
			this.mCircleProgressPath = new Path();
			this.mCircleProgressPath.addArc(mCircleRectF, mStartAngle, this.mProgressDegrees);
			if (CUSTOM_COLOR) {
				this.mCircleProgressPaint = new Paint();
				this.mCircleProgressPaint.setAntiAlias(true);
				this.mCircleProgressPaint.setDither(true);
				this.mCircleProgressPaint.setColor(this.progressColor);
				this.mCircleProgressPaint.setStrokeWidth(mCircleStrokeWidth);
				this.mCircleProgressPaint.setStyle(Paint.Style.STROKE);
				this.mCircleProgressPaint.setStrokeJoin(Paint.Join.ROUND);
				this.mCircleProgressPaint.setStrokeCap(Paint.Cap.ROUND);

				this.mCircleProgressGlowPaint = new Paint();
				this.mCircleProgressGlowPaint.set(this.mCircleProgressPaint);
				this.mCircleProgressGlowPaint.setMaskFilter(new BlurMaskFilter((5f * DPTOPX_SCALE), BlurMaskFilter.Blur.NORMAL));

				this.mPointerPaint = new Paint();
				this.mPointerPaint.setAntiAlias(true);
				this.mPointerPaint.setDither(true);
				this.mPointerPaint.setStyle(Paint.Style.FILL);
				this.mPointerPaint.setColor(this.pointerColor);
				this.mPointerPaint.setStrokeWidth(mPointerRadius);

				this.mPointerHaloPaint = new Paint();
				this.mPointerHaloPaint.set(this.mPointerPaint);
				this.mPointerHaloPaint.setColor(this.pointerColor);
				this.mPointerHaloPaint.setAlpha(mPointerAlpha);
				this.mPointerHaloPaint.setStrokeWidth(mPointerRadius + mPointerHaloWidth);

				this.mPointerHaloBorderPaint = new Paint();
				this.mPointerHaloBorderPaint.set(this.mPointerPaint);
				this.mPointerHaloBorderPaint.setStrokeWidth(mPointerHaloBorderWidth);
				this.mPointerHaloBorderPaint.setStyle(Paint.Style.STROKE);
			}
			else {
				this.mCircleProgressPaint = seek.mCircleProgressPaint;
				this.mCircleProgressGlowPaint = seek.mCircleProgressGlowPaint;
				this.mPointerPaint = seek.mPointerPaint;
				this.mPointerHaloPaint = seek.mPointerHaloPaint;
				this.mPointerHaloBorderPaint = seek.mPointerHaloBorderPaint;
			}
		}

        /**
         * Calculate the degrees that the progress represents. Also called the sweep angle.
         * Sets mProgressDegrees to that value.
         */
        protected void calculateProgressDegrees() {
            this.mProgressDegrees = this.mPointerPosition - mStartAngle; // Verified
            this.mProgressDegrees = (this.mProgressDegrees < 0 ? 360f + this.mProgressDegrees : this.mProgressDegrees); // Verified
        }

        /**
         * Calculate the pointer position (and the end of the progress arc) in degrees.
         * Sets mPointerPosition to that value.
         */
        protected void calculatePointerAngle() {
            float progressPercent = ((float)mProgress / (float)mMax);
            this.mPointerPosition = (progressPercent * mTotalCircleDegrees) + mStartAngle;
            this.mPointerPosition = this.mPointerPosition % 360f;
        }

        protected void setProgressBasedOnAngle(float angle) {
            this.mPointerPosition = angle;
            calculateProgressDegrees();
            this.mProgress = Math.round((float)mMax * this.mProgressDegrees / mTotalCircleDegrees);
        }

		protected float getAngle() {
			return this.mProgressDegrees;
		}

        /**
         * Get the progress of the CircularSeekBar.
         * @return The progress of the CircularSeekBar.
         */
        public int getProgress() {
            int progress = Math.round((float)mMax * mProgressDegrees / mTotalCircleDegrees);
            return progress;
        }


	}

}
