clear; close all; clc;

%% monitoring
clear; close all; clc;
delete(instrfindall);
s=serial('COM12', 'BaudRate', 115200)
fopen(s);
time=100;

threshold=struct('largemin', 1.2e+4, 'smallmin', 0, 'smallmax', 0, 'largemax', 2.5e+4);
max=struct('last', threshold.largemax, 'time', 0, 'sw', 0);
min=struct('last', threshold.largemin, 'time', 0, 'sw', 0);
seq=0;

figure(1);
grid on;
% subplot(2,1,1);
title('Accelerometer');
xlabel('time');
ylabel('Accelerometer(Magnitude)');
% subplot(2,1,2);
% title('Gyroscope');

a=zeros(1,1e+4);
for i=101:1e+4
    time=time+1;
    a(1,i)=str2num(fscanf(s));
    a(1,i)
    
    switch seq
        case 0,
            if a(1,i) > threshold.largemax
             if a(1,i) > max.last
                 max.last=a(1,i);
                
             elseif a(1,i) < max.last
                  max.time=i-1;
                  min.last=threshold.largemin;
                  min.time=0;
                  seq=1;
              end
            end
        case 1,
            if a(1,i) < threshold.largemin
               if a(1,i) < min.last
                 min.last=a(1,i);
            
               elseif a(1,i) > min.last
                 min.time=i-1;
                 max.last=threshold.largemax;
                 max.time=0;
                 seq=0;
               end
            end
            
    end
    
%     subplot(2,1,1);
    plot(time-100:time, a(i-100:i), 'r*-', ...
         [time-100,time], [threshold.largemin, threshold.largemin], 'b-', ...
         [time-100,time], [threshold.largemax, threshold.largemax], 'b-', ...
         min.time, min.last, 'bo', ...
         max.time, max.last, 'bo');
%     hold on;
%     plot(time-100:time, threshold.largemin, 'b-');
%     hold off;
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

%% distance measurement
clear; close all; clc;
delete(instrfindall);
port=serial('COM12', 'BaudRate', 115200)
fopen(port);

x = struct('isrising', 1, 'max', 0, 'min', 0, ...
           'found', 0, 'threshold', 0, 'count', 0);

time=100;

a=zeros(1e+4, 5);
for i=101:1e+4
    time=time+1;
    a(i,:)=str2num(fscanf(port));

%     timeout 최소 시간은 0.2초, 최대 시간은 2초로
% %     이전 측정과 시간이 비슷할 것 10%
%     최소 가속도 범위는 3
%     측정 우선순위 x -> y -> z
    if x.isrising
        if a(i,1)>a(i,5) + 2
            x.isrising = 0;
        end
    else
        if a(i,1)<a(i,5)-2
            x.isrising=1;
            x.count=x.count+1
        end
    end
    
    
    plot(time-100:time, a(i-100:i,1), 'r*-', ...
         time-100:time, a(i-100:i,2), 'g*-', ...
         time-100:time, a(i-100:i,3), 'b*-', ...
         time-100:time, a(i-100:i,5), 'k*-');
     xlim([time-100, time]);
%      ylim([-4e+4, 6e+4]);
    ylim([-20,20]);
    grid;
    drawnow;
    
%     velocity = velocity + dt*a(i,1:3);
%     pos = pos + velocity*dt
% 
%     plot3(pos(1), pos(2), 0, 'b*'); grid on;
% %     xlim([-10, 10]); ylim([-10, 10]); zlim([-10, 10]);
%     drawnow;
end