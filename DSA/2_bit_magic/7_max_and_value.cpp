#include<bits/stdc++.h>
using namespace std;

//Given an array arr[] of positive integers, You have to find the maximum AND value obtained by any pair (arr[i], arr[j]) such that i ≠ j .
//https://www.geeksforgeeks.org/batch/dsa-4/track/DSASP-BitMagic/problem/maximum-and-value-1587115620

int maxAND(vector<int>& arr) {
  vector<vector<int>> bit_matrix(32);
  for(int x = 0; x<arr.size(); x++){
    for(int i=0; i<32; i++){
      int mask = 1<<i;
      if(mask & arr[x]){
        bit_matrix[i].push_back(x);
      }
    }
  }

  for(int i=0; i<32; i++){
    cout << i <<": ";
    for(int x: bit_matrix[i])
      cout<<x<<" "; 
    cout<<"\n";
  }

  vector<int> lst;
  for(int i=31; i>=0; i--){
    if(bit_matrix[i].size() >= 2){
      lst = bit_matrix[i];
      break;
    }
  }

  int ans = 0;
  for(int i=0; i<lst.size(); i++){
    for(int j=i+1; j<lst.size(); j++){
      ans = max(ans, arr[lst[i]] & arr[lst[j]]);
    }
  }
  return ans;
}

int maxAND_optimum(const vector<int>& arr) {
    int res = 0;
        for(int bit=31; bit>=0; bit--){
            int candidate = res | (1<<bit);
            int count = 0;
            for(int x: arr){
                if((x&candidate) == candidate)
                    count++;
            }
            if(count>=2)
                res = candidate;
        }
        return res;
}


int main(int argc, char const *argv[]){
  vector<int> arr = {4,9,12,16,1,1};
  cout<<maxAND_optimum(arr)<<"\n";
  return 0;
}