import time, os, colorsys ,threading, random

pinRed   = 18
pinGreen = 23
pinBlue  = 24


def fade2(speed , stop_event):
    h = 0
    speed = (101-speed)/200

    while not stop_event.is_set():
        rgb = colorsys.hsv_to_rgb(h, 1, 1)
        setLED(rgb[0], rgb[1], rgb[2])

        h += 0.001

        if h > 1:
            h -= 1

        time.sleep(speed)


def strobe(speed, stop_event):
    speed = (101-speed)/2000

    while not stop_event.is_set():
        setLED(1, 1, 1)
        time.sleep(speed)
        setLED(0,0,0)
        time.sleep(speed)


def police(speed, stop_event):
    speed = (101-speed)/300
    print(speed)

    while not stop_event.is_set():
        setLED(1, 0, 0)
        time.sleep(speed)
        setLED(0,0,1)
        time.sleep(speed)


def party(speed, stop_event):
    speed = (101-speed)/2000

    while not stop_event.is_set():
        h = random.random()
        rgb = colorsys.hsv_to_rgb(h, 1, 1)
        setLED(rgb[0], rgb[1], rgb[2])
        time.sleep(speed)
        setLED(0,0,0)
        time.sleep(speed)


def setLED(r=-1, g=-1, b=-1):
    if r >= 0: os.system("echo 18="+str(r)+" > /dev/pi-blaster")
    if g >= 0: os.system("echo 23="+str(g)+" > /dev/pi-blaster")
    if b >= 0: os.system("echo 24="+str(b)+" > /dev/pi-blaster")


print("Set 1")
broadcastThread_stop = threading.Event()
broadcastThread = threading.Thread(target=police, args=(1, broadcastThread_stop), name='thread_function')
broadcastThread.start()

print("Wait")
time.sleep(5)
broadcastThread_stop.set()
setLED(0,0,0)
time.sleep(2)


print("Set 2")
broadcastThread_stop = threading.Event()
broadcastThread = threading.Thread(target=police, args=(50, broadcastThread_stop), name='thread_function')
broadcastThread.start()

print("Wait")
time.sleep(5)
broadcastThread_stop.set()
setLED(0,0,0)
time.sleep(2)


print("Set 3")
broadcastThread_stop = threading.Event()
broadcastThread = threading.Thread(target=police, args=(100, broadcastThread_stop), name='thread_function')
broadcastThread.start()

time.sleep(5)
broadcastThread_stop.set()
setLED(0,0,0)


