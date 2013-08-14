
#include <stdio.h>
#include <windows.h>
#include <GameUX.h>
#include <Objbase.h>
#include <stdlib.h>

int main() {
	
	malloc(10000000);
	
    
	IGameExplorer* pGE;
	//BOOL* accessAllowed = new BOOL(0);
	char* gdfLocation = "%USERPROFILE%\\Documents\\Programming\\Java\\Games\\DeductionTactics\\src\\main\\gdf\\installGdf.exe\0";
	BSTR gdfLocation2 = SysAllocStringLen(NULL, strlen(gdfLocation));
	
	HRESULT hr = CoCreateInstance( __uuidof(GameExplorer), NULL, CLSCTX_INPROC_SERVER, 
			__uuidof(IGameExplorer), (void**) &pGE );
	
	printf("Hello!");
	
	int len = MultiByteToWideChar( 1252, 0, gdfLocation, -1, 0, 0);
	
	
	// hr = pGE->VerifyAccess(gdfLocation2, accessAllowed);
	
	//if (accessAllowed) {
	//	  printf("Allowed");
	//} else {
	//	  printf("Not Allowed");
	//}
	
	printf("%s", gdfLocation);
	
	
	printf("\n");
	return 0;
}
