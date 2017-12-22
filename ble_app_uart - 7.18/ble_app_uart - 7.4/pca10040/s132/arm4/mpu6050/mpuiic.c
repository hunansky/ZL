#include "mpuiic.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "stdio.h"

#if defined BOARD_3_1
#define MPU_SCL_PIN 3
#define MPU_SDA_PIN 2
#elif defined BOARD_3_0
#define MPU_SCL_PIN 13
#define MPU_SDA_PIN 12
#endif
#define MPU_SDA_OUT() {nrf_gpio_cfg_output(MPU_SDA_PIN);}
#define MPU_SDA_IN()  {nrf_gpio_cfg_input(MPU_SDA_PIN,NRF_GPIO_PIN_NOPULL);}
#define MPU_READ_SDA()  nrf_gpio_pin_read(MPU_SDA_PIN)

#define MPU_SDA_H {nrf_gpio_pin_set(MPU_SDA_PIN);}
#define MPU_SDA_L {nrf_gpio_pin_clear(MPU_SDA_PIN);}
#define MPU_SCL_H {nrf_gpio_pin_set(MPU_SCL_PIN);}
#define MPU_SCL_L {nrf_gpio_pin_clear(MPU_SCL_PIN);}

/***************************************************************************************/

void mpu_iic_init(void)
{	   
	nrf_gpio_cfg_output(MPU_SCL_PIN);
	nrf_gpio_cfg_output(MPU_SDA_PIN);
	
	nrf_gpio_pin_clear(16);//MPU_INT clear
}

void MPU_IIC_DELAY(void)
{
   nrf_delay_us(1);
}


//����IIC��ʼ�ź�
void MPU_IIC_Start(void)
{
	MPU_SDA_OUT();     //sda�����
	MPU_SDA_H;	  	  
	MPU_SCL_H;
 	MPU_SDA_L;//START:when CLK is high,DATA change form high to low 
	MPU_IIC_DELAY();
	MPU_SCL_L;//ǯסI2C���ߣ�׼�����ͻ�������� 
}	  
//����IICֹͣ�ź�
void MPU_IIC_Stop(void)
{
	MPU_SDA_OUT();//sda�����
	MPU_SCL_L;
	MPU_SDA_L;//STOP:when CLK is high DATA change form low to high
 	MPU_IIC_DELAY();
	MPU_SCL_H;  
	MPU_SDA_H;//����I2C���߽����ź�
	MPU_IIC_DELAY();							   	
}
//�ȴ�Ӧ���źŵ���
//����ֵ��1������Ӧ��ʧ��
//        0������Ӧ��ɹ�
uint8_t MPU_IIC_Wait_Ack(void)
{
	uint8_t ucErrTime=0;
	MPU_SDA_IN();      //SDA����Ϊ����  
	MPU_SDA_H;MPU_IIC_DELAY();	   
	MPU_SCL_H;MPU_IIC_DELAY();	 
	while(MPU_READ_SDA())
	{
		ucErrTime++;
		if(ucErrTime>250)
		{
			MPU_IIC_Stop();
			return 1;
		}
	}
	MPU_SCL_L;//ʱ�����0 	   
	return 0;  
} 
//����ACKӦ��
void MPU_IIC_Ack(void)
{
	MPU_SCL_L;
	MPU_SDA_OUT();
	MPU_SDA_L;
	MPU_IIC_DELAY();
	MPU_SCL_H;
	MPU_IIC_DELAY();
	MPU_SCL_L;
}
//������ACKӦ��		    
void MPU_IIC_NAck(void)
{
	MPU_SCL_L;
	MPU_SDA_OUT();
	MPU_SDA_H;
	MPU_IIC_DELAY();
	MPU_SCL_H;
	MPU_IIC_DELAY();
	MPU_SCL_L;
}					 				     
//IIC����һ���ֽ�
//���شӻ�����Ӧ��
//1����Ӧ��
//0����Ӧ��			  
void MPU_IIC_Send_Byte(uint8_t txd)
{                        
    uint8_t t;   
	MPU_SDA_OUT(); 	    
    MPU_SCL_L;//����ʱ�ӿ�ʼ���ݴ���
    for(t=0;t<8;t++)
    {              
        if((txd&0x80)>>7)
				{
				  MPU_SDA_H;
				}
				else MPU_SDA_L;
        txd<<=1; 	  
		MPU_SCL_H;
		MPU_IIC_DELAY(); 
		MPU_SCL_L;	
		MPU_IIC_DELAY();
    }	 
} 	    
//��1���ֽڣ�ack=1ʱ������ACK��ack=0������nACK   
uint8_t MPU_IIC_Read_Byte(unsigned char ack)
{
	unsigned char i,receive=0;
	MPU_SDA_IN();//SDA����Ϊ����
    for(i=0;i<8;i++ )
	{
        MPU_SCL_L; 
        MPU_IIC_DELAY();
		    MPU_SCL_H;
        receive<<=1;
        if(MPU_READ_SDA())receive++;   
		MPU_IIC_DELAY(); 
  }					 
    if (!ack)
        MPU_IIC_NAck();//����nACK
    else
        MPU_IIC_Ack(); //����ACK   
    return receive;
}



//IICдһ���ֽ� 
//reg:�Ĵ�����ַ
//data:����
//����ֵ:0,����
//    ����,�������
uint8_t Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data) 				 
{ 
  MPU_IIC_Start(); 
	MPU_IIC_Send_Byte((dev_addr<<1)|0);//����������ַ+д����	
	if(MPU_IIC_Wait_Ack())	//�ȴ�Ӧ��
	{
		MPU_IIC_Stop();		 
		return 1;		
	}
    MPU_IIC_Send_Byte(reg_addr);	//д�Ĵ�����ַ
    MPU_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
	MPU_IIC_Send_Byte(data);//��������
	if(MPU_IIC_Wait_Ack())	//�ȴ�ACK
	{
		MPU_IIC_Stop();	
		return 1;		 
	}		 
    MPU_IIC_Stop();	 
	return 0;
}
//IIC��һ���ֽ� 
//reg:�Ĵ�����ַ 
//����ֵ:����������
uint8_t Read_Byte(uint8_t dev_addr,uint8_t reg_addr)
{
	uint8_t res;
  MPU_IIC_Start(); 
	MPU_IIC_Send_Byte((dev_addr<<1)|0);//����������ַ+д����	
	MPU_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
  MPU_IIC_Send_Byte(reg_addr);	//д�Ĵ�����ַ
  MPU_IIC_Wait_Ack();		//�ȴ�Ӧ��
  MPU_IIC_Start();
	MPU_IIC_Send_Byte((dev_addr<<1)|1);//����������ַ+������	
  MPU_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
	res=MPU_IIC_Read_Byte(0);//��ȡ����,����nACK 
  MPU_IIC_Stop();			//����һ��ֹͣ���� 
	return res;		
}






