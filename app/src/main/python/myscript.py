#!/usr/bin/env python
# coding: utf-8

from scipy.optimize import fsolve
# 算法
import math

#计算结果
def jisuan(J1,LPQ,LPC,LQC,LB1B,LAB,LPD,LAP,LDG,LD1D,LG1G,LEG,LED1,LHI,LGH,α,m,n,s):
    # 要测的变量
    # J1 = float(data['J1'])       # 角1
    # LPQ = float(data['LPQ'])     # PQ
    # LPC = float(data['LPC'])     # PC
    # LQC = float(data['LQC'])     # QC
    # LB1B = float(data['LB1B'])   # B'B
    # LAB = float(data['LAB'])     # AB
    # LPD = float(data['LPD'])     # PD
    # LAP = float(data['LAP'])     # AP
    # LDG = float(data['LDG'])     # DG 大液压缸
    # LD1D = float(data['LD1D'])   # D'D
    # LG1G = float(data['LG1G'])   # G'G
    # LEG = float(data['LEG'])     # EG
    # # LD1G1=1 #D'G' #平行关系 不需要知道D'G' 小液压缸
    # LED1 = float(data['LED1'])  # ED'
    # LHI = float(data['LHI'])    #HI
    # LGH = float(data['LGH'])  # GH
    LIG = LHI + LGH  #IG
    # α = float(data['α'])  # 角α（斗臂车底座旋转时，陀螺仪的旋转角度）
    # m = float(data['m'])  # O,O'的水平距离(斗臂车底座O 到 云台O'的水平距离，从正面看，云台O'延长水平线到斗臂车底座O截止，就是m)
    # n = float(data['n'])  # O,O'的竖直距离(斗臂车底座O 到 云台O'竖直距离，从正面看，云台O'和斗臂车底座O向上延长线，就是n)
    # s = float(data['s'])  # O,O'的横向距离(斗臂车底座O 到 云台O'的横向距离,从上向下看，云台O'和斗臂车底座O不是平行的，这个平行距离就是s)

    #换算
    pi = math.pi
    COSJP = (LPQ ** 2 + LPC ** 2 - LQC ** 2) / (2 * LPQ * LPC)
    JP = (math.acos(COSJP) / pi) * 180
    J3 = JP - J1
    a1 = LB1B * math.cos(J3 * pi / 180) - LAB * math.cos(J1 * pi / 180)
    b1 = LB1B * math.sin(J3 * pi / 180) - LAB * math.sin(J1 * pi / 180)
    a2 = LPD * math.cos(J3 * pi / 180) - LAP * math.cos(J1 * pi / 180)
    b2 = LPD * math.sin(J3 * pi / 180) - LAP * math.sin(J1 * pi / 180)
    k1 = (b2 - b1) / (a2 - a1)
    x1 = a2 + math.sqrt(LDG ** 2 / (k1 ** 2 + 1))
    y1 = k1 * (x1 - a2) + b2
    k2 = 0  #设置为0即可
    k3 = 0  #设置为0即可

    #解方程,注意k2 k3是方程未知数，需要求解的，需要单独传参；  后面参数都是方程的常量，需要和未知数分开传参
    solved = fsolve(solve_function, [k2,k3],[k1,x1,y1,a2,b2,LG1G,LD1D,LEG,LED1])

    k2 = solved[0]  #k2只是配合得出k3的结果
    k3 = solved[1]
    OM = x1 + math.sqrt(LIG ** 2 / (k3 ** 2 + 1))
    IM = k3 * OM + y1 - k3 * x1
    x = OM * math.cos(α * pi / 180) + m
    y = OM * math.sin(α * pi / 180) + s
    z = IM - n

    #data = "xAxis：%f,yAxis：%f,zAxis：%f" % (x, y, z); #返回前端展示
    return {'x':x,'y':y,'z':z}

#解方程
def solve_function(unsolved_value,cl):
    k2 = unsolved_value[0]
    k3 = unsolved_value[1]
    k1 = cl[0]
    x1 = cl[1]
    y1 = cl[2]
    a2 = cl[3]
    b2 = cl[4]
    LG1G = cl[5]
    LD1D = cl[6]
    LEG = cl[7]
    LED1 = cl[8]
    return [
        k1*(x1-math.sqrt(LG1G**2/(k3**2+1)))+k2*(a2-math.sqrt(LD1D**2/(k2**2+1)))+b2-k2*a2-k1*(a2-math.sqrt(LD1D**2/(k2**2+1)))-(k3*(x1-math.sqrt(LG1G**2/(k3**2+1)))+y1-k3*x1),
        ((x1-math.sqrt(LEG**2/(k3**2+1)))-(a2-math.sqrt(LD1D**2/(k2**2+1))))**2+(k3*(x1-math.sqrt(LEG**2/(k3**2+1)))+y1-k3*x1-k2*(a2-math.sqrt(LD1D**2/(k2**2+1)))-b2+k2*a2)**2-LED1**2,
        ]

if __name__ == '__main__':
    # data  = {"J1":"30","LPQ":"1.5","LPC":"2.5","LQC":"2","LB1B":"4","LAB":"2","LPD":"4","LAP":"1.75","LDG":"1.7","LD1D":"0.3","LG1G":"0.35","LEG":"1.8","LED1":"0.3","LHI":"1","LGH":"3.5","α":"30","m":"2","n":"0.5","s":"0.2"}
    jisuan(30,1.5,2.5,2,4,2,4,1.75,1.7,0.3,0.35,1.8,0.3,1,3.5,30,2,0.5,0.2)

def add(a,b):
    return a+b
def sub(a,b):
    return a-b