#include "../../apps.h"

static void button0_event(grf_ctrl_t *ctrl, grf_event_e event)
{
	 switch (event) {
	 	case GRF_EVENT_CLICKED:
	 		grf_reg_com_send(0x11,8);
	 		break;
	 }
}



static void button1_event(grf_ctrl_t *ctrl, grf_event_e event)
{
//	switch (event) {
//		case GRF_EVENT_CLICKED:{
//
//		}break;
//	}
}


static void button5_event(grf_ctrl_t *ctrl, grf_event_e event)
{
//	switch (event) {
//		case GRF_EVENT_CLICKED:{
//
//		}break;
//	}
}


static void button4_event(grf_ctrl_t *ctrl, grf_event_e event)
{
	switch (event) {
		case GRF_EVENT_CLICKED:{
			grf_view_set_dis_view_anim(GRF_CANPAGE_ID,GRF_SCR_LOAD_ANIM_FADE_IN, 600, 0,GRF_ANIM_PATH_END_SLOW);
		}break;
	}
}


static void label0_event(grf_ctrl_t *ctrl, grf_event_e event)
{
//	switch (event) {
//		case GRF_EVENT_CLICKED:{
//
//		}break;
//	}
}

#include "../../../libs/appscc/view1_cc.h"
void view1_init(void)
{
	grf_view_create(GRF_VIEW1_ID, view_ctrls_fun_t,sizeof(view_ctrls_fun_t) / sizeof(grf_ctrl_fun_t));
}

void view1_entry(void) 
{

}

void view1_exit(void)
{
	
}
