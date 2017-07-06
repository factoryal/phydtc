#include <SoftwareSerial.h>
#include <Wire.h>
#include <SPI.h>
#include <MPU9250.h>

// 핀에 관한 정의
#define LED 13
#define BTN 7
#define S1TX A4
#define S2RX A5

// Vector3 클래스
// 3차원 벡터를 관리하는 클래스입니다.
class Vector3 {
private:
	float x = 0;
	float y = 0;
	float z = 0;

public:
	// constructor
	Vector3();
	Vector3(const Vector3 &v) : x(v.x), y(v.y), z(v.z) {};
	Vector3(const float &_x, const float &_y, const float &_z) : x(_x), y(_y), z(_z) {};


	// setter
	void set(Vector3 &v) { x = v.x; y = v.y; z = v.z; }
	void setX(const float &_x) { x = _x; }
	void setY(const float &_y) { y = _y; }
	void setZ(const float &_z) { z = _z; }
	void setdX(const float &_x) { x += _x; }
	void setdY(const float &_y) { y += _y; }
	void setdZ(const float &_z) { z += _z; }


	// getter
	float getX() { return x; }
	float getY() { return y; }
	float getZ() { return z; }
	float getMagnitude() { return sqrt(x*x + y*y + z*z); }
	Vector3 getDirection() { return *this / getMagnitude(); }


	// 벡터 연산
	Vector3 operator+(const Vector3 &v) { return Vector3(x + v.x, y + v.y, z + v.z); }
	Vector3 operator-(const Vector3 &v) { return Vector3(x - v.x, y - v.y, z - v.z); }

	Vector3 operator+(const float &d) { return Vector3(x + d, y + d, z + d); }
	Vector3 operator-(const float &d) { return Vector3(x - d, y - d, z - d); }

	void operator+=(const Vector3 &v) { x += v.x; y += v.y; z += v.z; }
	void operator-=(const Vector3 &v) { x -= v.x; y -= v.y; z -= v.z; }

	void operator+=(const float &d) { x += d; y += d; z += d; }
	void operator-=(const float &d) { x -= d; y -= d; z -= d; }

	Vector3 operator*(const float &d) { return Vector3(x*d, y*d, z*d); }
	Vector3 operator/(const float &d) { return Vector3(x / d, y / d, z / d); }

	void operator*=(const float &d) { x *= d; y *= d; z *= d; }
	void operator/=(const float &d) { x /= d; y /= d; z /= d; }

	float dotTo(const Vector3 &v) { return x*v.x + y*v.y + z*v.z; }
	Vector3 crossTo(const Vector3 &v) { return Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x); }


	// Serial print
	void print() {
		Serial.print("xyz: ");
		Serial.print(x);
		Serial.print(' ');
		Serial.print(y);
		Serial.print(' ');
		Serial.println(z);
	}

	// destructor(auto)
	// ~Vector3();
};

// GY9250 클래스
// 9축(가속도3축, 자이로3축, 자기장3축) 센서를 관리하는 클래스입니다.
class GY9250 {
private:
	Vector3 a;
	Vector3 g;
	Vector3 m;

	enum SENSOR {

	};

public:
	GY9250();

	// 센서 선택
	int activate() {}
};


SoftwareSerial BT(A5, A4);


// 전역 변수
Vector3 v(1, 1, 1);


void buttonPressed() {
	static uint32_t oldTime = 0, newTime = 0;
	newTime = millis();
	if (newTime - oldTime > 100) {
		oldTime = newTime;
		v += 1;
		Serial.println(v.getX());
		BT.print(v.getX());
	}
}

// main()
void setup() {
	Serial.begin(9600);
	while (!Serial);
	Serial.println("Serial ready.");
	Serial.print("Initiallizing BT Serial port...");
	BT.begin(9600);
	Serial.print("OK");
	attachInterrupt(digitalPinToInterrupt(BTN), buttonPressed, FALLING);
	
	pinMode(BTN, INPUT_PULLUP);
	pinMode(13, OUTPUT);
}

void loop() {
}
