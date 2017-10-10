package edu.gwu.gwtrivia.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.gwu.gwtrivia.R
import edu.gwu.gwtrivia.utils.PersistanceManager
import com.google.android.gms.maps.model.LatLngBounds

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var persistanceManager: PersistanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        persistanceManager = PersistanceManager(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val builder = LatLngBounds.Builder()

        val scores = persistanceManager.fetchScores()


        for(score in scores){
            if(score.latitude != null && score.longitude != null) {
                val latLng = LatLng(score.latitude,score.longitude)
                map.addMarker(MarkerOptions().position(latLng).title("${getString(R.string.score)}: ${score.score}"))

                builder.include(latLng)
            }
        }

        val bounds = builder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0)
        map.moveCamera(cameraUpdate)
    }
}
