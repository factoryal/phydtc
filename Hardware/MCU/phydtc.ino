//#include <MemoryFree.h>
#include <MPU9250.h>
#include <I2Cdev.h>
#include <SoftwareSerial.h>
//#include <Wire.h>
#include <SPI.h>
//#include <RBL_services.h>
//#include <RBL_nRF8001.h>

float fmap(float, float, float, float, float);

// 설정에 관한 정의
#define WAIT_SERIAL_STARTUP 0
#define ANDROID_BT_PAIR_TEST 1

// 상수에 관한 정의
#define ts 0.03
#define tau 1/(0.8*2*3.14)
#define ttau 1/(0.5*2*3.14)
#define ttt ts/(tau+ts)
#define tttt ts/(ttau+ts)

// 핀에 관한 정의
#define LED_G A0
#define LED_R 13
#define BTN 7
#define S1TX A4
#define S2RX A5

// Vector3 클래스
// 3차원 벡터를 관리하는 클래스입니다.
class Vector3 {
private:
	typedef float vt;
	vt x = 0;
	vt y = 0;
	vt z = 0;

public:
	// constructor
	Vector3() : x(0), y(0), z(0) {};
	Vector3(const Vector3 &v) : x(v.x), y(v.y), z(v.z) {};
	Vector3(const vt &_x, const vt &_y, const vt &_z) : x(_x), y(_y), z(_z) {};


	// setter

	void set(const Vector3 &v) { x = v.x; y = v.y; z = v.z; }
	void set(const vt &_x, const vt &_y, const vt &_z) { x = _x; y = _y; z = _z; }
	void setX(const vt &_x) { x = _x; }
	void setY(const vt &_y) { y = _y; }
	void setZ(const vt &_z) { z = _z; }
	void setdX(const vt &_x) { x += _x; }
	void setdY(const vt &_y) { y += _y; }
	void setdZ(const vt &_z) { z += _z; }


	// getter

	vt getX() { return x; }
	vt getY() { return y; }
	vt getZ() { return z; }
	// 벡터의 크기를 구합니다.
	float getMagnitude() { return sqrt(x*x + y*y + z*z); } 

	// 벡터의 단위 벡터를 구합니다.
	Vector3 getDirection() { return *this / getMagnitude(); } 

	// x축과 벡터 사이의 각도의 cos 값을 구합니다.
	float getDirCosAlpha() { return (float)x / getMagnitude(); } 

	// y축과 벡터 사이의 각도의 cos 값을 구합니다.
	float getDirCosBeta() { return (float)y / getMagnitude(); }  

	// z축과 벡터 사이의 각도의 cos 값을 구합니다.
	float getDirCosGamma() { return (float)z / getMagnitude(); } 


	// 벡터 연산

	Vector3 operator+(const Vector3 &v) { return Vector3(x + v.x, y + v.y, z + v.z); }
	Vector3 operator-(const Vector3 &v) { return Vector3(x - v.x, y - v.y, z - v.z); }

	Vector3 operator+(const vt &d) { return Vector3(x + d, y + d, z + d); }
	Vector3 operator-(const vt &d) { return Vector3(x - d, y - d, z - d); }

	void operator+=(const Vector3 &v) { x += v.x; y += v.y; z += v.z; }
	void operator-=(const Vector3 &v) { x -= v.x; y -= v.y; z -= v.z; }

	void operator+=(const vt &d) { x += d; y += d; z += d; }
	void operator-=(const vt &d) { x -= d; y -= d; z -= d; }

	Vector3 operator*(const vt &d) { return Vector3(x*d, y*d, z*d); }
	Vector3 operator/(const vt &d) { return Vector3(x / d, y / d, z / d); }

	void operator*=(const vt &d) { x *= d; y *= d; z *= d; }
	void operator/=(const vt &d) { x /= d; y /= d; z /= d; }

	vt dotTo(const Vector3 &v) { return x*v.x + y*v.y + z*v.z; }
	Vector3 crossTo(const Vector3 &v) { return Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x); }


	// Serial print
	void printxyz() {
		Serial.print(x);
		Serial.print(' ');
		Serial.print(y);
		Serial.print(' ');
		Serial.print(z);
	}

	void printxyzln() { printxyz(); Serial.println(); }

	void printcos() {
		Serial.print(getDirCosAlpha());
		Serial.print(' ');
		Serial.print(getDirCosBeta());
		Serial.print(' ');
		Serial.print(getDirCosGamma());
	}


	// destructor(auto)
	// ~Vector3();
};


enum SENSOR {
	ACCEL = 1,
	GYRO = 2,
	MAGNETRO = 4
};

// GY9250 클래스
// 9축(가속도3축, 자이로3축, 자기장3축) 센서를 관리하는 클래스입니다.
class GY9250 {
private:
	Vector3 a;
	Vector3 ba;
	Vector3 ta;
	Vector3 tba;
	Vector3 g;
	Vector3 bg;
	Vector3 m;
	Vector3 init_a;
	int16_t buffer[9] = { 0 };
	struct {
		bool isrising[3] = { 1,1,1 };
		uint32_t count = 0;
	} c; //xyzcount
	MPU9250 MPU9250;

	uint8_t sensorEnabled = 0x00;


public:
	int block = 1;
	// constructor
	/*GY9250() {
	}*/

	bool init() {
		Serial.println(F("GY9250 initializing...."));
		MPU9250.initialize();
		delay(1000);
		Serial.print(F("Device ID: ")); Serial.println(MPU9250.getDeviceID(), HEX);
		return MPU9250.testConnection();
	}

	// 센서 선택
	uint8_t activate(uint8_t en) {
		sensorEnabled = en;
		if (sensorEnabled&ACCEL) {
			MPU9250.setStandbyXAccelEnabled(0);
			MPU9250.setStandbyYAccelEnabled(0);
			MPU9250.setStandbyZAccelEnabled(0);
		}
		if (sensorEnabled&GYRO) {
			MPU9250.setStandbyXGyroEnabled(0);
			MPU9250.setStandbyYGyroEnabled(0);
			MPU9250.setStandbyZGyroEnabled(0);
		}
		Serial.print(F("Sensor enabled: ")); Serial.print(sensorEnabled&ACCEL); Serial.println(sensorEnabled&GYRO);
		return sensorEnabled;
	}

	// enable 된 센서의 값을 읽고 업데이트합니다.
	void updateData(void) {
		MPU9250.getMotion6(buffer, buffer + 1, buffer + 2, buffer + 3, buffer + 4, buffer + 5);
		if (sensorEnabled&ACCEL) {
			a.set(fmap(buffer[0], 18750, -13950, 9.8, -9.8), fmap(buffer[1], 17800, -15000, 9.8, -9.8), fmap(buffer[2], 12875, -20550, 9.8, -9.8));// a /= 16384;
			a = ba + (a - ba)*ttt;
			ta = tba + (a - tba)*tttt;
			ba = a;
			tba = ta;
		}
		if (sensorEnabled&GYRO) {
			g.set(buffer[3], buffer[4], buffer[5]);// g /= 16384;
			g = bg + (g - bg)*ttt;
			bg = g;
		}
	}

	void countService() {
		if (c.isrising[0]) {
			if (a.getX() > ta.getX() + 1) {
				c.isrising[0] = false;
			}
		}
		else {
			if (a.getX() < ta.getX() - 1) {
				c.isrising[0] = true;
				c.count++;
				return;
			}
		}
		//if (c.isrising[1]) {
		//	if (a.getY() > ta.getY() + 2) {
		//		c.isrising[1] = false;
		//	}
		//}
		//else {
		//	if (a.getY() < ta.getY() - 2) {
		//		c.isrising[1] = true;
		//		c.count++;
		//		return;
		//	}
		//}
		//if (c.isrising[2]) {
		//	if (a.getZ() > ta.getZ() + 2) {
		//		c.isrising[2] = false;
		//	}
		//}
		//else {
		//	if (a.getZ() < ta.getZ() - 2) {
		//		c.isrising[2] = true;
		//		c.count++;
		//		return;
		//	}
		//}
		
	}

	uint32_t getCount() {
		return c.count;
	}

	// Serial prints
	void printAccelerometer() {
		/*Serial.print(a.getMagnitude());*/
		Serial.print(a.getX());
		Serial.print(' ');
		Serial.print(a.getY());
		Serial.print(' ');
		Serial.print(a.getZ());
		Serial.print(' ');
		Serial.print(a.getMagnitude());
		Serial.print(' ');
		Serial.print(ta.getX());
	}
	void printGyroscope() {
		Serial.print(g.getX());
		Serial.print(' ');
		Serial.print(g.getY());
		Serial.print(' ');
		Serial.print(g.getZ());
		Serial.print(' ');
		Serial.print(g.getMagnitude());
	}


} GY9250;



// LED 클래스
// LED의 상태를 관리하는 클래스입니다.
class LED {
private:
	uint8_t R;
	uint8_t G;
	uint8_t status;

public:
	LED(uint8_t red, uint8_t green) : R(red), G(green) {};

	void update() {
		status & 0x01 ? digitalWrite(R, 0) : digitalWrite(R, 1);
		status & 0x02 ? digitalWrite(G, 0) : digitalWrite(G, 1);
	}

	void set() {}

};


// 전역 변수 ------------

SoftwareSerial BT(A5, A4);
uint32_t val = 0x00;
uint32_t mov = 0;

float fmap(float x, float in_min, float in_max, float out_min, float out_max)
{
	return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}


// 버튼이 눌리면 실행되는 함수
void btn_pressed() {
	static uint32_t oldTime = 0, nowTime = 0;
	nowTime = millis();
	if (nowTime - oldTime < 150) return;
	Serial.println("btn time on");
	oldTime = nowTime;

	//여기에 코드 입력
	val++;
}




// main()
void setup() {
	pinMode(LED_R, OUTPUT);
	pinMode(LED_G, OUTPUT);
	pinMode(BTN, INPUT_PULLUP);
	digitalWrite(LED_R, HIGH);
	digitalWrite(LED_G, HIGH);
	attachInterrupt(digitalPinToInterrupt(BTN), btn_pressed, FALLING);

	Serial.begin(115200);
#if WAIT_SERIAL_STARTUP
	while (!Serial) {
		if (!digitalRead(BTN)) break;
	}
#endif
	Serial.println(F("Serial ready."));
	Serial.print(F("Initiallizing BT Serial port..."));
	BT.begin(9600);
	Serial.println("OK");
	while (!GY9250.init()) {
		Serial.println(F("GY9250 connection failed."));
	}
	Serial.println(F("GY9250 connection successfully."));
	GY9250.activate(ACCEL | GYRO);
	
	// 타이머 인터럽트 활성화
	cli();
	TIMSK3|= 1 << TOIE3; // 16-bit timer
	TCCR3A;
	TCCR3B |= 1 << CS31 | 1 << CS30; // clk/64
	sei();

}

#if ANDROID_BT_PAIR_TEST
void loop() {
	char buf[10];
	if (!GY9250.block) {
		static uint32_t oldTime1 = millis();
		if (millis() - oldTime1 > 30) {
			oldTime1 = millis();
			digitalWrite(LED_G, 0);
			GY9250.updateData();
			GY9250.printAccelerometer();
			Serial.println();
			GY9250.countService();
			digitalWrite(LED_G, 1);
		}

		static uint32_t oldTime2 = millis();
		if (millis() - oldTime2 > 1000) {
			String willsend;
			char buf[10];
			oldTime2 = millis();
			digitalWrite(LED_R, 0);
			val = GY9250.getCount();
			/*memset(buf, 0x1111, 2);
			memcpy(buf + 2, &val, 4);
			memcpy(buf + 6, &mov, 4);
			for (int i = 0; i < sizeof(buf); i++) BT.write(buf[i]);*/
			willsend += "1111";
			willsend += '/';
			willsend += itoa(val, buf, 10);
			willsend += '/';
			willsend += '0';
			willsend += '\n';
			BT.print(willsend);

			digitalWrite(LED_R, 1);
			for (int i = 0; i < sizeof(willsend); i++) BT.write(willsend[i]);
		}

		GY9250.block = 1;
	}
}

#else
void loop() {
	if (!GY9250.block) {
		static uint32_t oldTime1 = millis();
		if (millis() - oldTime1 > 30) {
			oldTime1 = millis();
			digitalWrite(LED_G, 0);
			GY9250.updateData();
			GY9250.printAccelerometer();
			Serial.println();
			GY9250.countService();
			digitalWrite(LED_G, 1);
		}

		static uint32_t oldTime2 = millis();
		if (millis() - oldTime2 > 1000) {
			oldTime2 = millis();
			digitalWrite(LED_R, 0);
			val = GY9250.getCount();
			BT.write(val);
			digitalWrite(LED_R, 1);
		}

		GY9250.block = 1;
	}
	
	// measure service
	// timein 범위: 100ms~3000ms
	// min delta*t: 3

}
#endif

// 타이머 인터럽트 실행 내용
ISR(TIMER3_OVF_vect) {
	GY9250.block = 0;
}