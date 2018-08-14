package io.github.hologuide.hlgservice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import static android.content.Context.LOCATION_SERVICE;

public class GPSLocationListener implements LocationListener
{
    public GPSLocationListener()
    {
        Activity activity = UnityPlayer.currentActivity;

        if (ActivityCompat.checkSelfPermission(activity.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity.getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            /** fine location のリクエストコード（値は他のパーミッションと被らなければ、なんでも良い）*/
            final int requestCode = 1;

            // いずれも得られていない場合はパーミッションのリクエストを要求する
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode );

            return;
        }

        // 位置情報を管理している LocationManager のインスタンスを生成する
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        String locationProvider = null;

        // GPSが利用可能になっているかどうかをチェック
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
        {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(settingsIntent);

            return;
        }

        // 利用可能なロケーションプロバイダによる位置情報の取得の開始
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);

    }

    private static final long minTime = 500; // 位置情報の通知するための最小時間間隔（ミリ秒）
    private static final long minDistance = 1; // 位置情報を通知するための最小距離間隔（メートル）

    @Override
    public void onLocationChanged(Location location)
    {
        ServiceManager.OnLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }
}
