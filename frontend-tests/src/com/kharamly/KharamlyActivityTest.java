package com.kharamly;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.kharamly.KharamlyActivityTest \
 * com.kharamly.tests/android.test.InstrumentationTestRunner
 */
public class KharamlyActivityTest extends ActivityInstrumentationTestCase2<KharamlyActivity> {
	KharamlyActivity mActivity;
    SlidingPanel mPanel;
	LinearLayout content;

	
    public KharamlyActivityTest() {
        super("com.kharamly", KharamlyActivity.class);
    }
	
	public void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mPanel = (SlidingPanel) mActivity.findViewById(com.kharamly.R.id.panel);
		content =  = (LinearLayout) mActivity.findViewById(com.kharamly.R.id.content);
	}
	
	public void testPreconditions() {
		assertNotNull(mActivity);
		assertNotNull(mPanel);		
		assertNotNull(content);		
	}
}
