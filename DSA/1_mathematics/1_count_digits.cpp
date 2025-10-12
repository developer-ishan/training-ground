#include <bits/stdc++.h>
using namespace std;

int main(int argc, char const *argv[])
{
  int x;
  cin >> x;
  int count = 0;
  while(x){
    x = x/10;
    count++;
  }
  cout << count << endl;
  return 0;
}
