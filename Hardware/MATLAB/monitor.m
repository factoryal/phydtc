clear; close all; clc;

delete(instrfindall);
s=serial('COM7', 'BaudRate', 115200)
fopen(s);
time=100;
figure(1);
subplot(2,1,1);
title('Accelerometer');
subplot(2,1,2);
title('Gyroscope');
a=zeros(10000, 6);
for i=1:10000
    time=time+1;
    a(i,:)=str2num(fscanf(s));
    subplot(2,1,1);
    hold on;
    plot([time-100: time], a(time-100:time,1), 'r*-');
    plot([time-100: time], a(time-100:time,2), 'g*-');
    plot([time-100: time], a(time-100:time,3), 'b*-');
%     plot(time, sqrt(a(1)^2+a(2)^2+a(3)^2), 'r*');
    xlim([time-200, time-100]);
%     
%     subplot(2,1,2);
%     hold on;
%     plot([time-100: time], a(time-100:time,4), 'r*-');
%     plot([time-100: time], a(time-100:time,5), 'g*-');
%     plot([time-100: time], a(time-100:time,6), 'b*-');
%     xlim([time-200, time-100]);
    drawnow;
end