package ch.heigvd.iict.sym_labo4;

<<<<<<< HEAD
=======
import android.content.Context;
>>>>>>> 01c4f3d64ba1bbc9642a8bc47c92786a858c0061
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

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mMagneticField = null;
    //opengl
    private OpenGLRenderer  opglr           = null;
    private GLSurfaceView   m3DView         = null;

<<<<<<< HEAD
    private float geoMagnet[] = null;
    private float gravity[] = null;
    private float roationMatrix[] = null;
=======
    private SensorManager sensoreManager    = null;
    private Sensor magnetometreSensor       = null;
    private Sensor accelerometreSensor      = null;

    private float magnetometreRot[]         = new float[16];
    private float accelerometreRot[]        = new float[16];

    private float magnetometreVar[]         = new float[3];
    private float accelerometreVar[]        = new float[3];
>>>>>>> 01c4f3d64ba1bbc9642a8bc47c92786a858c0061

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

<<<<<<< HEAD
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
=======
        sensoreManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if(sensoreManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometreSensor = sensoreManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensoreManager.registerListener(this, accelerometreSensor, sensoreManager.SENSOR_DELAY_NORMAL);
        }

        if(sensoreManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            magnetometreSensor = sensoreManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensoreManager.registerListener(this, magnetometreSensor, sensoreManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometreVar = event.values;
        }
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magnetometreVar = event.values;
        }

        SensorManager.getRotationMatrix(accelerometreRot, null, accelerometreVar, magnetometreVar);
        magnetometreRot = opglr.swapRotMatrix(accelerometreRot);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

>>>>>>> 01c4f3d64ba1bbc9642a8bc47c92786a858c0061
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
