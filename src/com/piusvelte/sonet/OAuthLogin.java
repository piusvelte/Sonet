/*
 * Sonet - Android Social Networking Widget
 * Copyright (C) 2009 Bryan Emmanuel
 * 
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Bryan Emmanuel piusvelte@gmail.com
 */
package com.piusvelte.sonet;

import static com.piusvelte.sonet.Sonet.TWITTER;
import static com.piusvelte.sonet.Sonet.TWITTER_URL_ACCESS;
import static com.piusvelte.sonet.Sonet.TWITTER_URL_AUTHORIZE;
import static com.piusvelte.sonet.Sonet.TWITTER_URL_REQUEST;
import static com.piusvelte.sonet.Tokens.TWITTER_KEY;
import static com.piusvelte.sonet.Tokens.TWITTER_SECRET;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import static com.piusvelte.sonet.Sonet.BUZZ;
import static com.piusvelte.sonet.Sonet.BUZZ_SCOPE;
import static com.piusvelte.sonet.Sonet.BUZZ_URL_ACCESS;
import static com.piusvelte.sonet.Sonet.BUZZ_URL_AUTHORIZE;
import static com.piusvelte.sonet.Sonet.BUZZ_URL_REQUEST;
import static com.piusvelte.sonet.Tokens.BUZZ_KEY;
import static com.piusvelte.sonet.Tokens.BUZZ_SECRET;

import static com.piusvelte.sonet.Sonet.MYSPACE;
import static com.piusvelte.sonet.Sonet.MYSPACE_URL_ACCESS;
import static com.piusvelte.sonet.Sonet.MYSPACE_URL_AUTHORIZE;
import static com.piusvelte.sonet.Sonet.MYSPACE_URL_REQUEST;
import static com.piusvelte.sonet.Tokens.MYSPACE_KEY;
import static com.piusvelte.sonet.Tokens.MYSPACE_SECRET;

import static com.piusvelte.sonet.Sonet.SALESFORCE;
import static com.piusvelte.sonet.Sonet.SALESFORCE_URL_ACCESS;
import static com.piusvelte.sonet.Sonet.SALESFORCE_URL_AUTHORIZE;
import static com.piusvelte.sonet.Sonet.SALESFORCE_URL_REQUEST;
import static com.piusvelte.sonet.Tokens.SALESFORCE_KEY;
import static com.piusvelte.sonet.Tokens.SALESFORCE_SECRET;

import com.piusvelte.sonet.Sonet.Accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthLogin extends Activity {
	private static final String TAG = "OAuthLogin";
	private static Uri TWITTER_CALLBACK = Uri.parse("sonet://twitter");
	private static Uri BUZZ_CALLBACK = Uri.parse("sonet://buzz");
	private static Uri MYSPACE_CALLBACK = Uri.parse("sonet://myspace");
	private static Uri SALESFORCE_CALLBACK = Uri.parse("sonet://salesforce");
	private SonetOAuth mSonetOAuth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int service = extras.getInt(Sonet.Accounts.SERVICE, Sonet.INVALID_SERVICE);
				SonetWebView sonetWebView = new SonetWebView();
				try {
					switch (service) {
					case TWITTER:
						mSonetOAuth = new SonetOAuth(TWITTER_KEY, TWITTER_SECRET);
						sonetWebView.open(mSonetOAuth.getAuthUrl(TWITTER_URL_REQUEST, TWITTER_URL_ACCESS, TWITTER_URL_AUTHORIZE, TWITTER_CALLBACK.toString(), true));
						break;
					case MYSPACE:
						mSonetOAuth = new SonetOAuth(MYSPACE_KEY, MYSPACE_SECRET);
						sonetWebView.open(mSonetOAuth.getAuthUrl(MYSPACE_URL_REQUEST, MYSPACE_URL_ACCESS, MYSPACE_URL_AUTHORIZE, MYSPACE_CALLBACK.toString(), true));
						break;
					case BUZZ:
						mSonetOAuth = new SonetOAuth(BUZZ_KEY, BUZZ_SECRET);
						sonetWebView.open(mSonetOAuth.getAuthUrl(BUZZ_URL_REQUEST + "?scope=" + URLEncoder.encode(BUZZ_SCOPE, "utf-8") + "&xoauth_displayname=" + getString(R.string.app_name) + "&domain=" + BUZZ_KEY, BUZZ_URL_ACCESS, BUZZ_URL_AUTHORIZE + "?scope=" + URLEncoder.encode(BUZZ_SCOPE, "utf-8") + "&xoauth_displayname=" + getString(R.string.app_name) + "&domain=" + BUZZ_KEY + "&btmpl=mobile", BUZZ_CALLBACK.toString(), true));
						break;
					case SALESFORCE:
						mSonetOAuth = new SonetOAuth(SALESFORCE_KEY, SALESFORCE_SECRET);
						sonetWebView.open(mSonetOAuth.getAuthUrl(SALESFORCE_URL_REQUEST, SALESFORCE_URL_ACCESS, SALESFORCE_URL_AUTHORIZE, SALESFORCE_CALLBACK.toString(), true) + "&oauth_consumer_key=" + SALESFORCE_KEY);
						break;
					default:
						this.finish();
					}
				} catch (OAuthMessageSignerException e) {
					Log.e(TAG,e.toString());
					this.finish();
				} catch (OAuthNotAuthorizedException e) {
					Log.e(TAG,e.toString());
					this.finish();
				} catch (OAuthExpectationFailedException e) {
					Log.e(TAG,e.toString());
					this.finish();
				} catch (OAuthCommunicationException e) {
					Log.e(TAG,e.toString());
					this.finish();
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG,e.toString());
					this.finish();
				}
			}
		}
	}

	private class SonetWebView {
		private WebView mWebView;

		public SonetWebView() {
			mWebView = new WebView(OAuthLogin.this);
			OAuthLogin.this.setContentView(mWebView);
			mWebView.setWebViewClient(new WebViewClient() {

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url != null) {
						Uri uri = Uri.parse(url);
						try {
							if (TWITTER_CALLBACK.getHost().equals(uri.getHost())) {
								mSonetOAuth.retrieveAccessToken(uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER));
								JSONObject jobj = new JSONObject(mSonetOAuth.get("http://api.twitter.com/1/account/verify_credentials.json"));
								ContentValues values = new ContentValues();
								values.put(Accounts.USERNAME, jobj.getString("screen_name"));
								values.put(Accounts.TOKEN, mSonetOAuth.getToken());
								values.put(Accounts.SECRET, mSonetOAuth.getTokenSecret());
								values.put(Accounts.EXPIRY, 0);
								values.put(Accounts.SERVICE, TWITTER);
								values.put(Accounts.TIMEZONE, 0);//tweets are in local time; //jobj.getString("utc_offset")
								values.put(Accounts.WIDGET, ManageAccounts.sAppWidgetId);
								if (ManageAccounts.sAccountId != Sonet.INVALID_ACCOUNT_ID) {
									getContentResolver().update(Accounts.CONTENT_URI, values, Accounts._ID + "=?", new String[]{Long.toString(ManageAccounts.sAccountId)});
									ManageAccounts.sAccountId = Sonet.INVALID_ACCOUNT_ID;
								} else getContentResolver().insert(Accounts.CONTENT_URI, values);
								ManageAccounts.sUpdateWidget = true;
							} else if (MYSPACE_CALLBACK.getHost().equals(uri.getHost())) {
								mSonetOAuth.retrieveAccessToken(uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER));
								String response = mSonetOAuth.get("http://opensocial.myspace.com/1.0/people/@me/@self");
								final String accountId;
								JSONObject jobj = new JSONObject(response);
								ContentValues values = new ContentValues();
								values.put(Accounts.USERNAME, jobj.getJSONObject("person").getString("displayName"));
								values.put(Accounts.TOKEN, mSonetOAuth.getToken());
								values.put(Accounts.SECRET, mSonetOAuth.getTokenSecret());
								values.put(Accounts.EXPIRY, 0);
								values.put(Accounts.SERVICE, MYSPACE);
								values.put(Accounts.TIMEZONE, 0);
								values.put(Accounts.WIDGET, ManageAccounts.sAppWidgetId);
								if (ManageAccounts.sAccountId != Sonet.INVALID_ACCOUNT_ID) {
									accountId = Long.toString(ManageAccounts.sAccountId);
									getContentResolver().update(Accounts.CONTENT_URI, values, Accounts._ID + "=?", new String[]{accountId});
									ManageAccounts.sAccountId = Sonet.INVALID_ACCOUNT_ID;
								} else accountId = getContentResolver().insert(Accounts.CONTENT_URI, values).getLastPathSegment();
								// get the timezone, index set to GMT
								OAuthLogin.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										(new AlertDialog.Builder(OAuthLogin.this))
										.setTitle(R.string.timezone)
										.setSingleChoiceItems(R.array.timezone_entries, 12, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												ManageAccounts.sUpdateWidget = true;
												ContentValues values = new ContentValues();
												values.put(Accounts.TIMEZONE, Integer.parseInt(getResources().getStringArray(R.array.timezone_values)[which]));
												getContentResolver().update(Accounts.CONTENT_URI, values, Accounts._ID + "=?", new String[]{accountId});
												dialog.cancel();
												// warn about new myspace permissions
												(new AlertDialog.Builder(OAuthLogin.this))
												.setTitle(R.string.myspace_permissions_title)
												.setMessage(R.string.myspace_permissions_message)
												.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
													public void onClick(DialogInterface dialog, int id) {
														dialog.cancel();
														OAuthLogin.this.finish();
													}
												})
												.show();
											}
										})
										.show();
									}
								});
								return true;
							} else if (BUZZ_CALLBACK.getHost().equals(uri.getHost())) {
								mWebView.setVisibility(View.INVISIBLE);
								mSonetOAuth.retrieveAccessToken(uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER));
								String username = new JSONObject(mSonetOAuth.get("https://www.googleapis.com/buzz/v1/people/@me/@self?alt=json")).getJSONObject("data").getString("displayName");
								if (username != null) {
									ContentValues values = new ContentValues();
									values.put(Accounts.USERNAME, username);
									values.put(Accounts.TOKEN, mSonetOAuth.getToken());
									values.put(Accounts.SECRET, mSonetOAuth.getTokenSecret());
									values.put(Accounts.EXPIRY, 0);
									values.put(Accounts.SERVICE, BUZZ);
									values.put(Accounts.TIMEZONE, 0);
									values.put(Accounts.WIDGET, ManageAccounts.sAppWidgetId);
									if (ManageAccounts.sAccountId != Sonet.INVALID_ACCOUNT_ID) {
										getContentResolver().update(Accounts.CONTENT_URI, values, Accounts._ID + "=?", new String[]{Long.toString(ManageAccounts.sAccountId)});
										ManageAccounts.sAccountId = Sonet.INVALID_ACCOUNT_ID;
									} else getContentResolver().insert(Accounts.CONTENT_URI, values);
									ManageAccounts.sUpdateWidget = true;
								}
							} else if (SALESFORCE_CALLBACK.getHost().equals(uri.getHost())) {
								mSonetOAuth.retrieveAccessToken(uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER));
								String response = mSonetOAuth.post("https://login.salesforce.com/services/OAuth/u/21.0");
								Log.v(TAG,"response:"+response);
								//account info
								//https://login.salesforce.com/ID/orgID/userID?Format=json
							} else if (uri.getHost().contains("salesforce.com") && (uri.getQueryParameter("oauth_consumer_key") == null)) {
								Log.v(TAG,"load:"+url + "&oauth_consumer_key=" + SALESFORCE_KEY);
								view.loadUrl(url + "&oauth_consumer_key=" + SALESFORCE_KEY);
								return true;
							} else return false;// allow google to redirect
						} catch (OAuthMessageSignerException e) {
							Log.e(TAG, e.getMessage());
						} catch (OAuthNotAuthorizedException e) {
							Log.e(TAG, e.getMessage());
						} catch (OAuthExpectationFailedException e) {
							Log.e(TAG, e.getMessage());
						} catch (OAuthCommunicationException e) {
							Log.e(TAG, e.getMessage());
						} catch (ClientProtocolException e) {
							Log.e(TAG, e.getMessage());
						} catch (JSONException e) {
							Log.e(TAG, e.getMessage());
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
					OAuthLogin.this.finish();
					return true;
				}

			});
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("UTF-8");
		}

		public void open(String url) {
			if (url != null) mWebView.loadUrl(url);
			else OAuthLogin.this.finish();
		}

	}

}