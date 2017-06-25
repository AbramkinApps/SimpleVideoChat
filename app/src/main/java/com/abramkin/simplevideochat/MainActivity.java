package com.abramkin.simplevideochat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import android.support.annotation.NonNull;
import android.Manifest;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, SessionData.Listener {


    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;

    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private TextView mTextView;

    private Stream mStreamWaiting;
    private SessionData sessionData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };

        //проверяем разрешения, привязываем элементы интерфейса, настраиваем сессию

        if (EasyPermissions.hasPermissions(this, perms)) {


            mTextView = (TextView) findViewById(R.id.textView);

            mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

            sessionData = new SessionData(this, this);
            sessionData.getSessionData("https://simplevideochat.herokuapp.com/session");

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onConnected(Session session) {

        //при подключении к сессии публикуем поток

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        // при получении нового потока отображаем его, либо запоминаем, если какой-либо поток уже отображается
        if (mSubscriber == null) {

            mTextView.setVisibility(View.INVISIBLE);

            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());

        } else {
            mStreamWaiting = stream;
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        //при прерывании потока отображаем waiting mode

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();

            mTextView.setVisibility(View.VISIBLE);
        }

        //если есть поток в режиме ожидания, отображаем его

        if (mStreamWaiting !=null) {

            mTextView.setVisibility(View.INVISIBLE);

            mSubscriber = new Subscriber.Builder(this, mStreamWaiting).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
            mStreamWaiting = null;
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

        Log.e(LOG_TAG, "PublisherKit error: " + opentokError.getMessage());

    }


    @Override
    public void initializeSession(String apiKey, String sessionId, String token) {
        mSession = new Session.Builder(this, apiKey, sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);
    }
}
