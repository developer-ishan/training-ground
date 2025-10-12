#include <bits/stdc++.h>
using namespace std;

int main(int argc, char const *argv[])
{
  int x;
  cin >> x;
  int rev = 0;
  int temp = x;
  while(temp){
    int digit = temp%10;
    temp/=10;
    rev = rev*10 + digit;
  }
  cout << rev << "\n"; 
  if(rev == x){
    cout<<"palindrome\n";
  } else {
    cout<<"not palindrome\n";
  }
}
