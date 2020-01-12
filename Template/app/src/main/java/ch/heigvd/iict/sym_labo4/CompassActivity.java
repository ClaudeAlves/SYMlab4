package ch.heigvd.iict.sym_labo4;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer;

/**
 * Modifications : Benjamin Thomas, Gabriel Arzur Catel Torres, Alves Claude-André
 * ajout de la foncitonnalité de boussole avec implémentation de onResume et onPause dans le cas ou
 * l'on changerai d'application.
 *
 */

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

        private SensorManager mSensorManager = null;
        private Sensor mAccelerometer = null;
        private Sensor mMagneticField = null;
        //opengl
        private OpenGLRenderer  opglr           = null;
        private GLSurfaceView   m3DView         = null;

        private float geoMagnet[] = null;
        private float gravity[] = null;
        private float roationMatrix[] = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            geoMagnet = new float[3];
            gravity = new float[3];
            roationMatrix = new float[16];

            // we need fullscreen
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // we initiate the view
            setContentView(R.layout.activity_compass);
            //we create the renderer
            this.opglr = new OpenGLRenderer(getApplicationContext());
            // link to GUI
            this.m3DView = findViewById(R.id.compass_opengl);
            //init opengl surface view
            this.m3DView.setRenderer(this.opglr);

            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        }
        protected void onResume() {
            super.onResume();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }

        protected void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                System.arraycopy(event.values, 0, geoMagnet, 0, geoMagnet.length);
            }
            else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                System.arraycopy(event.values, 0, gravity, 0, gravity.length);
            }
            mSensorManager.getRotationMatrix(roationMatrix, null, gravity, geoMagnet);
            opglr.swapRotMatrix(roationMatrix);
        }


    /* TODO
        your activity need to register to accelerometer and magnetometer sensors' updates
        then you may want to call
        this.opglr.swapRotMatrix()
        with the 4x4 rotation matrix, everytime a new matrix is computed
        more information on rotation matrix can be found on-line:
        https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],%20float[],%20float[],%20float[])
    */
    }