clear; close all; clc;

delete(instrfindall);
s=serial('COM5', 'BaudRate', 115200)
fopen(s);
time=100;
figure(1);
grid on;
% subplot(2,1,1);
title('Accelerometer');
xlabel('time');
% ylabel('Accelerometer(Magnitude)');
% subplot(2,1,2);
% title('Gyroscope');
a=zeros(1,100000);
for i=101:100000
    time=time+1;
    a(1,i)=str2num(fscanf(s));
%     subplot(2,1,1);
%     hold on;
    plot(time-100:time, a(i-100:i), 'r*-');
%     plot(time, a(2), 'g*');
%     plot(time, time^2*a(3)/2, 'b*');
%     plot(time, sqrt(a(1)^2+a(2)^2+a(3)^2), 'r*');
    xlim([time-100, time]);
%     
%     subplot(2,1,2);
%     hold on;
%     plot([time-100: time], a(time-100:time,4), 'r*-');
%     plot([time-100: time], a(time-100:time,5), 'g*-');
%     plot([time-100: time], a(time-100:time,6), 'b*-');
%     xlim([time-200, time-100]);
    drawnow;
end