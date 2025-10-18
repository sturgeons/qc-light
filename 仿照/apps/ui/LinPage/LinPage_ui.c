#include "../../apps.h"


#include "../../../libs/appscc/LinPage_cc.h"
void LinPage_init(void)
{
	grf_view_create(GRF_LINPAGE_ID,view_ctrls_fun_t,sizeof(view_ctrls_fun_t)/sizeof(grf_ctrl_fun_t));
}

void LinPage_entry(void)
{

}

void LinPage_exit(void)
{

}
