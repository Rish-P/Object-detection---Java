# Object-detection-Java
Perform object detection in pure Java using image processing and pixel clustering techniques
This java file contains the logic and code to implement object detection over on an input image containing several objects in the scene. Given multiple objects as string params, the code detects an object by drawing a bounding box around it and labelling it accordingly. The project uses histogram color matching technique to locate matching pixels with the object image and finally performs island-based clustering to isolate the detected object in the input image
