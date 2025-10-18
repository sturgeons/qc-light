#include "../../apps.h"
// 这里是一个带发送的命令列表
static u8 cmd_list[][23] = {

    // UART L Turn ADDR1
    {0x55, 0x42, 0x61, 0xCE, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},
    // UART L Turn ADDR2
    {0x55, 0x42, 0x62, 0x9E, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},
    // UART L Turn ADDR3
    {0x55, 0x42, 0xE3, 0x26, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81},
    // UART L Turn ADDR4
    {0x55, 0x42, 0x64, 0x3E, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},

    // UART L Turn ADDR5
    {0x55, 0x42, 0x65, 0xE2, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},

    // UART R Turn ADDR6
    {0x55, 0x42, 0x66, 0xB2, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},
    // UART R Turn ADDR7
    {0x55, 0x42, 0x67, 0x6E, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},

    // UART R Turn ADDR8
    {0x55, 0x42, 0x68, 0x4A, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},

    // UART R Turn ADDR9
    {0x55, 0x42, 0xE9, 0xF2, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81},
    // UART R Turn ADDR10
    {0x55, 0x42, 0x6A, 0xC6, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},
    // UART R Turn ADDR11
    {0x55, 0x42, 0x6B, 0x1A, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},
    // UART R Turn ADDR12
    {0x55, 0x42, 0x6C, 0x66, 0xFC, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
     0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0x22},

    // UART L Tail ADDR13
    {0x55, 0x7C, 0x2D, 0xA4, 0xFC, 0xFF, 0x03, 0xA0},
    // UART L Tail ADDR14
    {0x55, 0x7C, 0x2E, 0xF4, 0xFC, 0xFF, 0x03, 0xA0},
    // UART L Tail ADDR15
    {0x55, 0x7C, 0x2F, 0x28, 0xFC, 0xFF, 0x03, 0xA0},
    // UART L Tail ADDR16
    {0x55, 0x7C, 0x30, 0xBC, 0xFC, 0xFF, 0x03, 0xA0},
    // UART L Tail ADDR17
    {0x55, 0x7C, 0x31, 0x60, 0xFC, 0xFF, 0x03, 0xA0},
    // UART L Tail ADDR18
    {0x55, 0x7C, 0x32, 0x30, 0xFC, 0xFF, 0x03, 0xA0},
    // MID Tail19
    {0x55, 0x7C, 0x33, 0xEC, 0xFC, 0xFF, 0x03, 0xA0},
    // MID Tail20
    {0x55, 0x7C, 0x34, 0x90, 0xFC, 0x00, 0x00, 0x1D},

};

static int send_flag = 0;

void send_cmd(int index) {
  u8 *full_cmd = cmd_list[index];
  int total_len;
  // 根据命令索引确定总数据长度
  if (index == 2 || index == 8) {
    total_len = 21; // ADDR3和ADDR9命令长度为22字节(有0x81结尾)
  } else if (index >= 0 && index <= 11) {
    total_len = 19; // UART L/R Turn命令(ADDR1-12)长度为19字节
  } else {
    total_len = 8; // UART L Tail和MID Tail命令(ADDR13-20)长度为8字节
  }
  // 从第4位的0xFC处切割数据
  u8 *header = full_cmd; // header: 前4个字节
  int header_len = 4;
  u8 *data = full_cmd + 5;      // data: 0xFC之后的部分
  int data_len = total_len - 5; // 总长度 - 前5个字节(包括0xFC)
  grf_sline_send(header, header_len, data, data_len);
  grf_delay(20);
}
void send_cmd_all(int index) {
  u8 *full_cmd = cmd_list[index];
  int total_len;
  // 根据命令索引确定总数据长度
  if (index == 2 || index == 8) {
    total_len = 21; // ADDR3和ADDR9命令长度为22字节(有0x81结尾)
  } else if (index >= 0 && index <= 11) {
    total_len = 19; // UART L/R Turn命令(ADDR1-12)长度为19字节
  } else {
    total_len = 8; // UART L Tail和MID Tail命令(ADDR13-20)长度为8字节
  }
  // 从第4位的0xFC处切割数据
  u8 *header = full_cmd; // header: 前4个字节
  int header_len = 4;
  u8 *data = full_cmd + 5;      // data: 0xFC之后的部分
  int data_len = total_len - 5; // 总长度 - 前5个字节(包括0xFC)
  grf_sline_send(header, header_len, data, data_len);
  grf_delay(2);
}
static void button1_event(grf_ctrl_t *ctrl, grf_event_e event) {
  switch (event) {
  case GRF_EVENT_CLICKED: {
    for (int loop = 0; loop < 50; loop++) {
      // 发送所有20个命令
      for (int i = 0; i < 20; i++) {
    	  send_cmd_all(i);
      }
    }
  } break;
  }
}

static void button0_event(grf_ctrl_t *ctrl, grf_event_e event) {
  switch (event) {
  case GRF_EVENT_CLICKED: {
    grf_view_set_dis_view_anim(GRF_VIEW1_ID, GRF_SCR_LOAD_ANIM_FADE_IN, 600, 0,
                               GRF_ANIM_PATH_END_SLOW);
  } break;
  }
}

static void button2_event(grf_ctrl_t *ctrl, grf_event_e event) {
  switch (event) {
  case GRF_EVENT_CLICKED: {
    // 循环发送整个命令列表100遍
    for (int loop = 0; loop < 100; loop++) {
      send_cmd(0);
    }
  } break;
  }
}

static void label0_event(grf_ctrl_t *ctrl, grf_event_e event) {
  //	switch (event) {
  //		case GRF_EVENT_CLICKED:{
  //
  //		}break;
  //	}
}

static void sw0_event(grf_ctrl_t *ctrl, grf_event_e event) {
  switch (event) {
  case GRF_EVENT_CLICKED: {
    u8 *full_cmd = cmd_list[0];

    // 从第4位的0xFC处切割数据
    u8 *header = full_cmd; // header: 前4个字节
    int header_len = 4;

    u8 *data = full_cmd + 5; // data: 0xFC之后的部分
    int data_len = 8 - 5;    // 总长度8 - 前5个字节(包括0xFC) = 3

    grf_sline_send(header, header_len, data, data_len);
  } break;
  }
}

static void button3_event(grf_ctrl_t *ctrl, grf_event_e event) {
  //	switch (event) {
  //		case GRF_EVENT_CLICKED:{
  //
  //		}break;
  //	}
}

static void button22_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(19);
	    }
	  } break;
	  }
}

static void button21_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(15);
	    }
	  } break;
	  }
}

static void button20_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(11);
	    }
	  } break;
	  }
}

static void button19_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(7);
	    }
	  } break;
	  }
}

static void button18_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(18);
	    }
	  } break;
	  }
}

static void button17_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(14);
	    }
	  } break;
	  }
}

static void button16_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(10);
	    }
	  } break;
	  }
}

static void button15_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(6);
	    }
	  } break;
	  }
}

static void button14_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(17);
	    }
	  } break;
	  }
}

static void button13_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(13);
	    }
	  } break;
	  }
}

static void button12_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(9);
	    }
	  } break;
	  }
}

static void button11_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(5);
	    }
	  } break;
	  }
}

static void button10_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(16);
	    }
	  } break;
	  }
}

static void button9_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(12);
	    }
	  } break;
	  }
}

static void button8_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(8);
	    }
	  } break;
	  }
}

static void button7_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(4);
	    }
	  } break;
	  }
}

static void button6_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(3);
	    }
	  } break;
	  }
}

static void button5_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(2);
	    }
	  } break;
	  }
}

static void button4_event(grf_ctrl_t *ctrl, grf_event_e event) {
	 switch (event) {
	  case GRF_EVENT_CLICKED: {
	    // 循环发送整个命令列表100遍
	    for (int loop = 0; loop < 100; loop++) {
	      send_cmd(1);
	    }
	  } break;
	  }
}

#include "../../../libs/appscc/CanPage_cc.h"
void CanPage_init(void) {
  grf_view_create(GRF_CANPAGE_ID, view_ctrls_fun_t,
                  sizeof(view_ctrls_fun_t) / sizeof(grf_ctrl_fun_t));
}

void CanPage_entry(void) {}

void CanPage_exit(void) {}
