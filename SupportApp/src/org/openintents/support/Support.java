package org.openintents.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.donations.DonationsActivity;
import org.openintents.support.R;
import org.openintents.intents.SupportIntents;
import org.openintents.metadata.SupportMetaData;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.TabHost;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main About dialog activity.
 * 
 * @author pjv
 *
 */
public class Support extends TabActivity {

	private static final String TAG = "Support";
	private MetaDataReader metaDataReader;
	
	protected ImageSwitcher mLogoImage;
	protected ImageSwitcher mEmailImage;
	protected Button btLoad, btSubmit;
//	protected TextSwitcher mProgramNameAndVersionText;
	protected TextSwitcher mCommentsText;
	protected TextSwitcher mCopyrightText;
	protected TextSwitcher mWebsiteText;
	protected TextSwitcher mEmailText;
	protected TextView mAuthorsLabel;
	protected TextView mAuthorsText;
	protected TextView mDocumentersLabel;
	protected TextView mDocumentersText;
	protected TextView mTranslatorsLabel;
	protected TextView mTranslatorsText;
	protected TextView mArtistsLabel;
	protected TextView mArtistsText;
	protected TextView mInternationalTranslatorsLabel;
	protected TextView mInternationalTranslatorsText;
	protected TextView mNoInformationText;
//	protected TextView mLicenseText;
//	protected TextView mRecentChangesText;
	protected EditText input;
	protected TabHost tabHost;

	/**
	 * Menu item id's.
	 */
	public static final int MENU_ITEM_ABOUT = Menu.FIRST;


	/**
	 * Retrieve the package name to be used with this intent.
	 * 
	 * Package name is retrieved from EXTRA_PACKAGE or from
	 * getCallingPackage().
	 * 
	 * If none is supplied, it is set to this application.
	 */
	String getPackageNameFromIntent(Intent intent) {
		String packagename = null;
		
		if (intent.hasExtra(SupportIntents.EXTRA_PACKAGE_NAME)) {
			packagename = intent.getStringExtra(SupportIntents.EXTRA_PACKAGE_NAME);

			// Check whether packagename is valid:
			try {
	            getPackageManager().getApplicationInfo(
	            		packagename, 0);
		    } catch (NameNotFoundException e) {
		        Log.e(TAG, "Package name " + packagename + " is not valid.", e);
		        packagename = null;
		    }
		}
		
		// If no valid name has been found, we try to obtain it from
		// the calling activit.
		if (packagename == null) {
			// Retrieve from calling activity
			packagename = getCallingPackage();
		}
		
	    if (packagename == null) {
	    	// In the worst case, use our own name:
	    	packagename = getPackageName();
	    }
	    
	    return packagename;
	}

	private void submitVote(String outcome) {
	    HttpClient client = new DefaultHttpClient();
	    HttpPost post = new HttpPost("https://spreadsheets.google.com/spreadsheet/formResponse?hl=en_US&formkey=dDRIOWdEYXI1d1U1MURZdjlhVWdUOWc6MQ");

	    List<BasicNameValuePair> results = new ArrayList<BasicNameValuePair>();
	    results.add(new BasicNameValuePair("entry.0.single", outcome));
//	    results.add(new BasicNameValuePair("entry.1.single", outcome));
//	    results.add(new BasicNameValuePair("entry.2.single", cardTwoURL));

	    try {
	        post.setEntity(new UrlEncodedFormEntity(results));
	    } catch (UnsupportedEncodingException e) {
	        // Auto-generated catch block
	        Log.e("YOUR_TAG", "An error has occurred", e);
	    }
	    try {
	        client.execute(post);
	        Toast.makeText(Support.this, "Thanks! Your Feedback Submitted!" , Toast.LENGTH_LONG).show();
					
	    } catch (ClientProtocolException e) {
	        // Auto-generated catch block
	        Log.e("YOUR_TAG", "client protocol exception", e);
	    } catch (IOException e) {
	        // Auto-generated catch block
	        Log.e("YOUR_TAG", "io exception", e);
	    }
	}

	/**
	 * Get application label.
	 * 
	 * @param intent The intent from which to fetch the information.
	 */
	protected String getApplicationLabel(final String packagename, final Intent intent) {
		String applicationlabel = null;
		if (intent.hasExtra(SupportIntents.EXTRA_APPLICATION_LABEL)
				&& intent.getStringExtra(SupportIntents.EXTRA_APPLICATION_LABEL) 
					!= null) {
			applicationlabel = intent.getStringExtra(SupportIntents.EXTRA_APPLICATION_LABEL);
		} else {
            try {
                    PackageInfo pi = getPackageManager().getPackageInfo(
                    		packagename, 0);
                    int labelid = pi.applicationInfo.labelRes;
         			Resources resources = getPackageManager()
         					.getResourcesForApplication(packagename);
         			applicationlabel = resources.getString(labelid);
            } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Package name not found", e);
            }
		}
		return applicationlabel;
	}
	
	/* (non-Javadoc)
     * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy); 
    	
    	Resources res = getResources();
    	Log.d("identifier", res.getString(res.getIdentifier("string/about_translators", null, getPackageName())));
    		
    	Map<String, String> metaDataNameToTagName = new HashMap<String, String>();
    	//TODO Move this to detached file?
    	metaDataNameToTagName.put("comments", SupportMetaData.METADATA_COMMENTS);
    	metaDataNameToTagName.put("copyright", SupportMetaData.METADATA_COPYRIGHT);
    	metaDataNameToTagName.put("website-url", SupportMetaData.METADATA_WEBSITE_URL);
    	metaDataNameToTagName.put("website-label", SupportMetaData.METADATA_WEBSITE_LABEL);
    	metaDataNameToTagName.put("authors", SupportMetaData.METADATA_AUTHORS);
    	metaDataNameToTagName.put("documenters", SupportMetaData.METADATA_DOCUMENTERS);
    	metaDataNameToTagName.put("translators", SupportMetaData.METADATA_TRANSLATORS);
    	metaDataNameToTagName.put("artists", SupportMetaData.METADATA_ARTISTS);
    	metaDataNameToTagName.put("license", SupportMetaData.METADATA_LICENSE);
    	metaDataNameToTagName.put("email", SupportMetaData.METADATA_EMAIL);
    	metaDataNameToTagName.put("recent-changes", SupportMetaData.METADATA_RECENT_CHANGES);
    	
    	String packageName = getPackageNameFromIntent(getIntent());
    	try{
	    	metaDataReader = new MetaDataReader(getApplicationContext(),
	    			packageName,
	    			metaDataNameToTagName);
    	}catch(NameNotFoundException e){
    		throw new IllegalArgumentException("Package name '"+packageName+"' doesn't exist.");
    	}
    	
    	
    	//Set up the layout with the TabHost
    	tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.main,
				tabHost.getTabContentView(), true);
        
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_info))
                .setIndicator(getString(R.string.l_info))
                .setContent(R.id.sv_info));
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_credits))
                .setIndicator(getString(R.string.l_credits))
                .setContent(R.id.sv_credits));
        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_license))
                .setIndicator(getString(R.string.l_license))
                .setContent(R.id.sv_license));
        tabHost.addTab(tabHost.newTabSpec("Donate")
                .setIndicator("Donate")
                .setContent(R.id.sv_hello));

        // Add fourth tab only if "recent changes" information
        // has been provided
        final Intent intent = getIntent();
        if (intent == null) {
            setIntent(new Intent());
        }
        String packagename = getPackageNameFromIntent(intent);
//        if (hasRecentChanges(packagename, intent)) {
//	        tabHost.addTab(tabHost.newTabSpec(getString(R.string.l_recent_changes))
//	                .setIndicator(getString(R.string.l_recent_changes))
//	                .setContent(R.id.sv_recent_changes));
//        }
        
    	btLoad = (Button) findViewById(R.id.btApprove);
    	btLoad.setOnClickListener(new View.OnClickListener() {

    		public void onClick(View v) {
//    			endFixed = true;
    			startActivity(new Intent(Support.this, DonationsActivity.class));
    		}
    	});

    	input = (EditText) findViewById(R.id.txtIdea);
    	btSubmit = (Button) findViewById(R.id.btSubmit2);
    	btSubmit.setOnClickListener(new View.OnClickListener() {

      		public void onClick(View v) {
      		  submitVote(input.getText().toString()); 
      		}
      	});


    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//About action
		menu.add(ContextMenu.NONE, MENU_ITEM_ABOUT, ContextMenu.NONE,
				R.string.menu_about).setIcon(R.drawable.ic_menu_info_details);

		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, Support.class), null,
				intent, 0, null);

		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_ABOUT:
				// Show the about dialog for this app.
				showSupportDialog();
				return true;
			
			default:
				// Whoops, unknown menu item.
				Log.e(TAG, "Unknown menu item");
		}
		return super.onOptionsItemSelected(item);
	}
	

	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
        // tabHost.setCurrentTabByTag(getString(R.string.l_info));
		
		//Decode the intent, if any
		final Intent intent = getIntent();
		/*
        if (intent == null) {
        	refuseToShow();
        	return;
        }
        */
		if (intent == null) {
			setIntent(new Intent());
		}
		
		String packagename = getPackageNameFromIntent(intent);
    	setResult(RESULT_OK);
	}

	/**
	 * Show an about dialog for this application.
	 */
	protected void showSupportDialog() {
		Intent intent = new Intent(SupportIntents.ACTION_SHOW_ABOUT_DIALOG);
		
		// Start support activity. Needs to be "forResult" with requestCode>=0
		// so that the package name is passed properly.
		//
		// The details are obtained from the Manifest through
		// default tags and metadata.
		startActivityForResult(intent, 0);
	}
}
