#include<bits/stdc++.h>
using namespace std;

//https://www.geeksforgeeks.org/batch/dsa-4/track/DSASP-BitMagic/problem/number-is-sparse-or-not-1587115620

bool isSparse(int n) {
        // code here
        int mask = 1;
        for(int i=0;i<31; i++){
            if( (n&mask) > 0 && (n&(mask<<1)) > 0 )
                return false;
            mask = mask<<1;
        }
        return true;
    }

    
int main(int argc, char const *argv[])
{
  
  return 0;
}