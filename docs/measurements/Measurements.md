# Measurements Report

## Table of Contents

- [Measurements Report](#measurements-report)
  - [Table of Contents](#table-of-contents)
  - [Plots](#plots)
    - [Distance Measurements](#distance-measurements)
    - [Angle Measurements](#angle-measurements)
  - [Measurement process:](#measurement-process) 
  - [Final Notes:](#final-notes)

---

## Plots

### Distance Measurements
![Distance measurements](./Distance%20measurements.png)

---

### Angle Measurements

-   **For 0.1 m**  
    ![Angle Measurements for 0.1 m](./Angle%20measurements%20for%200.1%20m.png)

-   **For 0.25 m**  
    ![Angle Measurements for 0.25 m](./Angle%20measurements%20for%200.25%20m.png)

-   **For 0.5 m**  
    ![Angle Measurements for 0.5 m](./Angle%20measurements%20for%200.5%20m.png)

-   **For 0.75 m**  
    ![Angle Measurements for 0.75 m](./Angle%20measurements%20for%200.75%20m.png)

-   **For 1 m**  
    ![Angle Measurements for 1 m](./Angle%20measurements%20for%201%20m.png)

-   **For 1.5 m**  
    ![Angle Measurements for 1.5 m](./Angle%20measurements%20for%201.5%20%20m.png)

-   **For 2 m**  
    ![Angle Measurements for 2 m](./Angle%20measurements%20for%202%20m.png)

-   **For 2.5 m**  
    ![Angle Measurements for 2.5 m](./Angle%20measurements%20for%202.5%20m.png)

-   **For 3 m**  
    ![Angle Measurements for 3 m](./Angle%20measurements%20for%203%20m.png)

-   **For 3.5 m**  
    ![Angle Measurements for 3.5 m](./Angle%20measurements%20for%203.5%20m.png)

-   **For 4 m**  
    ![Angle Measurements for 4 m](./Angle%20measurements%20for%204%20m.png)

-   **For 4.5 m**  
    ![Angle Measurements for 4.5 m](./Angle%20measurements%20for%204.5%20m.png)

-   **For 5 m**  
    ![Angle Measurements for 5 m](./Angle%20measurements%20for%205%20m.png)

---

## Measurement process:

The phones were placed in a 3D printed measuring device (later reffed as cube), that allowed for setting them a certain angle, from -90&deg; to 90&deg;. One of the phones was rotated and physical angle measured by the cube was entered manually into a textfield in the Demo screen. The cubes with phones were placed different distances apart, the physical distance was measured using a measuring tape. Even alignment of the cubes towards each other relied on the even lineup of floor panels. The measured physical distance was then entered into a textfield. Textfield values and values returned by the uwb library were automatically collected for 10 seconds and send to a database. 

Devices:

-  **OBI measuring tape**: an extendable 5 m measuring tape, with accuracy class II (II klasa dokładności)
-  **3D printed cube**: for placing phones, photographed below
  ![The cube]()
  ![The measuring setup]()


## Final Notes:

As observed, when the phones are in very close proximity (0.1 m), both angle and distance measurements show significant inaccuracies. Specifically:

-   **Combined Distance RMSE**: 146.98
-   **Combined Angle RMSE**: 27.85

However, for distances greater than **0.25 m**, the measurements become more reliable:

-   **Distance Measurements RMSE**: 0.012 (submeter accuracy)
-   **Angle Measurements RMSE**: 25.29

From the charts, we can conclude that:

-   **Angle measurements** are quite accurate for angles between **-45** and **45 degrees**.
-   For extreme angles (e.g., **-90** and **90 degrees**), the accuracy decreases, and sometimes the measurements "jump" to significantly opposite values.

---
