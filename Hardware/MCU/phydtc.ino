#include <MemoryFree.h>
#include <MPU9250.h>
#include <SoftwareSerial.h>
//#include <Wire.h>
#include <SPI.h>

// 설정에 관한 정의
#define SERIAL_WAIT_STARTUP

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
	Vector3 g;
	Vector3 m;
	int16_t buffer[9] = { 0 };

	MPU9250 MPU9250;

	uint8_t sensorEnabled = 0x00;



public:
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
		if (sensorEnabled&ACCEL) a.set(buffer[0], buffer[1], buffer[2]); a /= 16384;
		if (sensorEnabled&GYRO) g.set(buffer[3], buffer[4], buffer[5]); g /= 16384;
	}

	// Serial prints
	void printAccelerometer() {
		a.printcos();
	}
	void printGyroscope() {
		g.printxyz();
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
uint16_t val = 0;


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
#ifdef WAIT_SERIAL_STARTUP
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
	/*cli();
	TIMSK3|= 1 << TOIE3;
	TCCR3A;
	TCCR3B = 0;
	TCCR3B |= 1 << CS32 | 1 << CS30;
	sei();*/
}

void loop() {
	GY9250.updateData();
	GY9250.printAccelerometer();
	Serial.print(' ');
	GY9250.printGyroscope();
	Serial.println();
	//BT.print(val);
	digitalWrite(LED_G, HIGH);
	delay(50);
	digitalWrite(LED_G, LOW);
	delay(50);
}

// 타이머 인터럽트 실행 내용
//ISR(TIMER3_COMPA_vect) {
//	Serial.println("hello world!");
//}