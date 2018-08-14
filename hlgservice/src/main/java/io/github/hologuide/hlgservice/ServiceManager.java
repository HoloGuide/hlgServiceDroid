package io.github.hologuide.hlgservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import com.unity3d.player.UnityPlayer;

public class ServiceManager
{
    public static volatile String GameObject = null;

    public void Initialize(String gameObject)
    {
        GameObject = gameObject;
    }

    public void StartService()
    {
        Activity activity = UnityPlayer.currentActivity;
        Context context = activity.getApplicationContext();
        activity.startService(new Intent(context, WebService.class));
    }

    public void StopService()
    {
        WebService.ShouldContinue = false;
    }

    public static void OnReceived(String message)
    {
        com.unity3d.player.UnityPlayer.UnitySendMessage(GameObject, "onReceived", message);
    }

    public static void SendBroadcast(String message)
    {
        WebSocketHandler.sendBroadcast(message);
    }

    public static void OnLocationChanged(Location location)
    {
        String json = null;

        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "location");
            jsonObject.put("lat", location.getLatitude());
            jsonObject.put("lng", location.getLongitude());
            jsonObject.put("updated", location.getTime());

            json = jsonObject.toString(4);

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (json != null)
        {
            SendBroadcast(json);
            com.unity3d.player.UnityPlayer.UnitySendMessage(GameObject, "onLocationChanged", json);
        }
    }

}
