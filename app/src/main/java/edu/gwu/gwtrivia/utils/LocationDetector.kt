package edu.gwu.gwtrivia.utils

import android.content.Context
import android.location.Location
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Created by jared on 10/8/17.
 */

class LocationDetector(val context: Context) {
    val fusedLocationClient: FusedLocationProviderClient

    init {
        fusedLocationClient = FusedLocationProviderClient(context)
    }

    //enum to describe reasons location detection might fail
    enum class FailureReason {
        TIMEOUT,
        NO_PERMISSION
    }

    var locationListener: LocationListener? = null

    interface LocationListener {
        fun locationFound(location: Location)
        fun locationNotFound(reason: FailureReason)
    }

    fun detectLocation() {
        //create location request
        val locationRequest = LocationRequest()
        locationRequest.interval = 0L

        //check for location permission
        val permissionResult = ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        //if location permission granted, proceed with location detection
        if(permissionResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            //create timer
            val timer = Timer()

            //create location detection callback
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    //stop location updates
                    fusedLocationClient.removeLocationUpdates(this)

                    //cancel timer
                    timer.cancel()

                    //fire callback with location
                    locationListener?.locationFound(locationResult.locations.first())
                }
            }

            //start a timer to ensure location detection ends after 10 seconds
            timer.schedule(timerTask {
                //if timer expires, stop location updates and fire callback
                fusedLocationClient?.removeLocationUpdates(locationCallback)
                locationListener?.locationNotFound(FailureReason.TIMEOUT)
            }, 10*1000) //10 seconds


            //start location updates
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null)
        }
        else {
            //else if no permission, fire callback
            locationListener?.locationNotFound(FailureReason.NO_PERMISSION)
        }
    }
}