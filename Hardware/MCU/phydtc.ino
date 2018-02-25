#include <TimeLib.h>
#include <MemoryFree.h>
#include <MPU9250.h>
#include <I2Cdev.h>
#include <SoftwareSerial.h>
//#include <Wire.h>
#include <SPI.h>
#include <RBL_services.h>
#include <RBL_nRF8001.h>

float fmap(float, float, float, float, float);
uint32_t atou32(const char* str);

// 설정에 관한 정의
#define WAIT_SERIAL_STARTUP 1
#define ANDROID_BT_PAIR_TEST 0
#define BT_NRF8001 0

// 상수에 관한 정의
#define BLE_NAME "Devsign"
#define ts 0.03
#define tau 1/(0.8*2*3.14)
#define ttau 1/(0.5*2*3.14)
#define ttt ts/(tau+ts)
#define tttt ts/(ttau+ts)

// 핀에 관한 정의
#define LED_G A0
#define LED_R 13
#define BTN 9
#define S1TX A4
#define S2RX 11
#define BAT_REMAIN A2
#define BAT_CHRG 8
#define BAT_STBY 5


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
		uint32_t lastTime1[3] = { 0 };
		uint32_t lastTime2[3] = { 0 };
		bool patternSwich[3] = { 0,0,0 };
		uint8_t countBuffer = 0;
		uint32_t count = 0;
	} c; //xyzcount
	MPU9250 M;

	uint8_t sensorEnabled = 0x00;


public:
	int block = 1;
	// constructor
	/*GY9250() {
	}*/

	bool init() {
		Serial.println(F("GY9250 initializing...."));
		M.initialize();
		delay(1000);
		Serial.print(F("Device ID: ")); Serial.println(M.getDeviceID(), HEX);
		//return M.testConnection();
		return 1;
	}

	// 센서 선택
	uint8_t activate(uint8_t en) {
		sensorEnabled = en;
		if (sensorEnabled&ACCEL) {
			M.setStandbyXAccelEnabled(0);
			M.setStandbyYAccelEnabled(0);
			M.setStandbyZAccelEnabled(0);
		}
		if (sensorEnabled&GYRO) {
			M.setStandbyXGyroEnabled(0);
			M.setStandbyYGyroEnabled(0);
			M.setStandbyZGyroEnabled(0);
		}
		Serial.print(F("Sensor enabled: ")); Serial.print(sensorEnabled&ACCEL); Serial.println(sensorEnabled&GYRO);
		return sensorEnabled;
	}

	// enable 된 센서의 값을 읽고 업데이트합니다.
	void updateData(void) {
		M.getMotion6(buffer, buffer + 1, buffer + 2, buffer + 3, buffer + 4, buffer + 5);
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
				uint32_t delta0 = (millis() - c.lastTime1[0]);
				uint32_t delta1 = (c.lastTime1[0] - c.lastTime2[0]);
				float deltaRatio = (float)delta1 / delta0;
				if (deltaRatio>0.80 && deltaRatio<1.20 && delta0<2000 && delta0>230) {
					c.countBuffer++;
					c.count += c.countBuffer;
					c.countBuffer = 0;
				}
				else {
					c.countBuffer = 2;
				}
				c.lastTime2[0] = c.lastTime1[0];
				c.lastTime1[0] = millis();
				
			}
		}
		return;
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
		
		c.count += c.countBuffer;
		c.countBuffer = 0;
	}

	uint32_t getCount() {
		return c.count;
	}

	void countInit() {
		c.count = 0;
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
	struct ledinfo{
		uint8_t pin;
		uint8_t s = 0;
		//(void)(*activator)(ledinfo, uint16_t);
	} led[2];


public:
	enum {
		RED, GREEN
	};

	LED(uint8_t red, uint8_t green) {
		led[0].pin = red; led[1].pin = green;
	}

	/*void update() {
		for (int i = 0; i < 2; i++) {
			led[i].activator();
			digitalWrite(led[i].pin, led[i].s);
		}
	}

	void set(uint8_t color, bool s) {}
	void blink(uint8_t color, uint16_t t) {
		led[color].activator = blinker;
	}*/

	void turn(uint8_t color, bool s) { digitalWrite(led[color].pin, s); }

} LED(LED_R, LED_G);


// BAT 클래스
// 배터리의 상태를 관리하는 클래스입니다.
class BAT {
private:
	uint8_t s = DRAIN;
	uint8_t remain = 0;

	uint8_t Li1SVto100p(uint16_t r) {
		float v = fmap(r, 0, 1023, 0, 3.3);
		v = fmap(v, 3.5 / 2, 4.19 / 2, 0, 100);
		return v > 0 ? v : 0;
	}

public:
	enum STATUS {
		DRAIN, CHRG, STBY
	};

	BAT() {
		pinMode(BAT_CHRG, 0);
		pinMode(BAT_STBY, 0);
	}

	uint8_t status() { return s; };
	uint8_t level() { return remain; };

	void update() {
		if (!digitalRead(BAT_CHRG)) s = CHRG;
		else if (!digitalRead(BAT_STBY)) s = STBY;
		else s = DRAIN;
		remain = Li1SVto100p(analogRead(BAT_REMAIN));
	}
} BAT;


// BLUETOOTH 클래스
// 개발보드의 경우, SoftwareSerial 클래스를 사용하고
// 프로토타입의 경우, BLUETOOTH 클래스를 사용합니다.
// BT_NRF8001 = 1이면 프로토타입, 아니면 개발보드입니다.
#if BT_NRF8001
class BLUETOOTH {
private: 
	uint8_t buf_len = 0;

public:
	void begin(long ignored) {
		ble_begin();
		ble_set_name(BLE_NAME);
	}
	
	void write(unsigned char data) { ble_write(data); }
	void writeBytes(unsigned char *data, uint8_t len) { ble_write_bytes(data, len); }
	int read() { return ble_read(); }
	unsigned char available() { return ble_available(); }
	unsigned char isConnected() { return ble_connected(); }
	void ble_write_string(byte *bytes, uint8_t len) {
		if (buf_len + len > 20)
		{
			for (int j = 0; j < 15000; j++)
				ble_do_events();

			buf_len = 0;
		}

		for (int j = 0; j < len; j++)
		{
			ble_write(bytes[j]);
			buf_len++;
		}

		if (buf_len == 20)
		{
			for (int j = 0; j < 15000; j++)
				ble_do_events();

			buf_len = 0;
		}
	}
	void print(unsigned long n, uint8_t base=10)
	{
		char buf[8 * sizeof(long) + 1]; // Assumes 8-bit chars plus zero byte.
		char *str = &buf[sizeof(buf) - 1];

		*str = '\0';

		// prevent crash if called with base == 1
		if (base < 2) base = 10;

		do {
			char c = n % base;
			n /= base;

			*--str = c < 10 ? c + '0' : c + 'A' - 10;
		} while (n);

		writeBytes((unsigned char *)str, sizeof(str));
	}
	//void end() { ble_disconnect(); }
} BT;
#else
SoftwareSerial BT(S2RX, S1TX);
#endif


// CountLog 클래스
// 카운트된 값을 저장하는 클래스입니다.
class CountLog {
private:
	struct table_count{
		time_t date = 0;
		uint32_t count = 0;
	} data;

	CountLog* before = NULL;
	CountLog* after = NULL;

public:
	// Data Structural Linker
	void setAfter(CountLog *t) { after = t; }
	void setBefore(CountLog *t) { before = t; }
	void linkBeforeTo(CountLog *t) {
		after = t;
		t->setBefore(this);
	}
	void linkAfterTo(CountLog *t) {
		before = t;
		t->setAfter(this);
	}
	CountLog* getBefore() { return before; }
	CountLog* getAfter() { return after; }


	// Data management 
	time_t setDate(time_t t) { data.date = t / 86400; }
	bool setCount(uint32_t c, time_t t = now()) {
		if (t / 86400 == data.date) {
			data.count = c;
			return true;
		}
		else if (before) before->setCount(c, t);
		return false;
	}
	time_t getDateSeconds() {
		return data.date * 86400;
	}
	uint32_t getCount() { return data.count; }

};


// CountQueue 클래스
// CountLog 클래스를 큐 자료구조로 관리하는 클래스입니다.
class CountQueue {
private:

public:

};

// 전역 ------------

uint32_t val = 0x00;
uint32_t mov = 0;
struct {
	char buf[20] = { 0 };
	byte idx = 0;
} BT_rx, BT_tx;
CountLog *front = new CountLog();
CountLog *rear = front, *tmp;



float fmap(float x, float in_min, float in_max, float out_min, float out_max)
{
	return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
uint32_t atou32(const char* str) {
	uint32_t willreturn = 0;
	for (int i = 0; str[i] >= '0' && str[i] <= '9'; i++) willreturn = 10 * willreturn + str[i] - '0';
	return willreturn;
}

// 버튼이 눌리면 실행되는 함수
void btn_pressed() {
	static uint32_t oldTime = 0, nowTime = 0;
	nowTime = millis();
	if (nowTime - oldTime < 150) return;
	Serial.println(strlen(BT_rx.buf));
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
	//attachInterrupt(digitalPinToInterrupt(BTN), btn_pressed, FALLING);

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
		static bool sw = false;
		if (millis() - oldTime1 > 30) {
			oldTime1 = millis();

			digitalWrite(LED_G, 0);
			GY9250.updateData();
			GY9250.printAccelerometer();
			Serial.println();
			GY9250.countService();
			digitalWrite(LED_G, 1);

			// btn falling interrupt (polling)
if (!digitalRead(BTN) && !sw) {
	btn_pressed();
	sw = true;
}
if (sw && digitalRead(BTN)) sw = false;
		}

		static uint32_t oldTime2 = millis();
		if (millis() - oldTime2 > 300) {
			char willsend[20] = { 0 };
			char buf[10];
			oldTime2 = millis();

			digitalWrite(LED_R, 0);
			val = GY9250.getCount();
			/*memset(buf, 0x1111, 2);
			memcpy(buf + 2, &val, 4);
			memcpy(buf + 6, &mov, 4);
			for (int i = 0; i < sizeof(buf); i++) BT.write(buf[i]);*/
			strcat(willsend, "1111");
			strcat(willsend, "/");
			strcat(willsend, itoa(val, buf, 10));
			strcat(willsend, "/");
			strcat(willsend, "0");
			strcat(willsend, "\n");
#if BT_NRF8001
			BT.writeBytes((unsigned char*)willsend, sizeof(willsend));
#else
			BT.print(willsend);
#endif

			digitalWrite(LED_R, 1);
			//for (int i = 0; i < sizeof(willsend); i++) BT.write(willsend[i]);
		}


		GY9250.block = 1;
	}
}

#else
void loop() {
	//Serial.println(freeMemory());
	if (!GY9250.block) {
		static bool sw = false;
		static uint32_t oldTime1 = millis();
		if (millis() - oldTime1 > 30) {
			oldTime1 = millis();
			digitalWrite(LED_G, 0);
			GY9250.updateData();
			//GY9250.printAccelerometer();
			//Serial.println();
			GY9250.countService();
			digitalWrite(LED_G, 1);

			// btn falling interrupt (polling)
			if (!digitalRead(BTN) && !sw) {
				btn_pressed();
				sw = true;
			}
			if (sw && digitalRead(BTN)) sw = false;
		}


		static uint32_t oldTime2 = millis();
		if (millis() - oldTime2 > 1000) {
			oldTime2 = millis();
			/*digitalWrite(LED_R, 0);
			val = GY9250.getCount();
			BT.write(val);
			digitalWrite(LED_R, 1);*/
			
			if (rear->getDateSeconds() + 86400 < now()) {
				CountLog* tmp = new CountLog();
				tmp->setDate(now());
				rear->linkBeforeTo(tmp);
				rear = tmp;
				GY9250.countInit();
			}
			rear->setCount(GY9250.getCount(), now());

			Serial.print("now(): ");
			Serial.println(now());
		}

		// if incoming data available...
		if (Serial.available()) {
			char c = Serial.read();
			BT_rx.buf[BT_rx.idx++] = c;

			if (c == '\n') { // if recieve data meets terminator
				BT_rx.buf[BT_rx.idx - 1] = '\0';
				Serial.print("A: ");
				Serial.print(BT_rx.buf);
				Serial.println();

				if (strstr(BT_rx.buf, "st")) {
					Serial.print("parsetime: ");
					Serial.println(BT_rx.buf + 3);
					Serial.print("len(btrxbuf): ");
					Serial.println(strlen(BT_rx.buf));
					setTime((time_t)atou32(BT_rx.buf + 3));
					memset(BT_rx.buf, 0, sizeof(BT_rx.buf));
				}

				else if (strstr(BT_rx.buf, "gc")) {
					do {
						uint64_t dateSeconds = front->getDateSeconds();
						Serial.print("D: ");
						Serial.print(year(dateSeconds));
						Serial.write('-');
						Serial.print(month(dateSeconds));
						Serial.write('-');
						Serial.print(day(dateSeconds));
						Serial.write('/');
						Serial.print(front->getCount());
						Serial.write('\n');
						BT.print(year(dateSeconds));
						BT.write('-');
						BT.print(month(dateSeconds));
						BT.write('-');
						BT.print(day(dateSeconds));
						BT.write('/');
						BT.print(front->getCount());
						delay(10);
						BT.write('@');
						delay(1);
						if (front->getAfter()) {
							front = front->getAfter();
							delete front->getBefore();
							front->setBefore(NULL);
						}
						else break;
					} while (1);
				}
				else if (strstr(BT_rx.buf, "gb")) {
				}

				BT_rx.idx = 0;
				memset(BT_rx.buf, 0, sizeof(BT_rx.buf));
			}
		}

		GY9250.block = 1;
	}

}
#endif


// 타이머 인터럽트 실행 내용
ISR(TIMER3_OVF_vect) {
	GY9250.block = 0;
}