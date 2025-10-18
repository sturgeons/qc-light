#ifndef __GRF_HW_UART_H_
#define __GRF_HW_UART_H_

#include "../../../inc/grf_typedef.h"








void grf_uart_init(void);
s32 grf_reg_set(u16 addr,u16 data);
s32 grf_reg_get(u16 addr);
s32 grf_reg_com_send(u16 addr,u16 len);
s32 grf_sline_send(u8* header, u32 header_len, u8* data, u32 data_len);



#endif
