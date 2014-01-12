import java.io.*;
import java.io.DataInputStream;

import java.util.*;
import java.util.Random;
import java.util.Arrays;

import java.lang.*;

import java.math.*;
import java.math.BigDecimal;




//instance of the class peer represents a peer

class peer
{

	
	//rating variables used by witnesses
	float trust;
	float untrust;
	float uncertainity;

	//rating variables used by Requesting agent
	float[] wit_trust = new float[25];
	float[] wit_untrust = new float[25];
	float[] wit_uncertainity = new float[25];
	
	//weights assigned to witnesses by Requesting agent
	float[] weights = new float[25];

	//Probability values of each witness calculated by Requesting agent
	float[] probTrust=new float[25];
	
	//list of neighbors for a particular peer
	ArrayList neighbor=new ArrayList();


	
	// used by witnesses to provide ratings
	void setRating(int a,int b,int c)
	{
		trust=(float)(a*(0.1));
		untrust=(float)(b*(0.1));
		uncertainity=(float)(c*(0.1));

	}


	//function to update the weights of witnesses 
	void updateWeight(int i,float wt)
	{
		
		weights[i]=wt*weights[i];
		BigDecimal bd = new BigDecimal(weights[i]);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		weights[i] = bd.floatValue();
	}
	
}


class p2p
{

	static int numPeers;
	static DataInputStream in =new DataInputStream(System.in);
	static peer[] p=new peer[25];

	static int[] randlist=new int[25];
	static Random rand=new Random();
	
	static ArrayList witlist=new ArrayList();
	static ArrayList visited=new ArrayList();

	static int ar,ag;
	static int count=0,limit=0;

	static int numNeighbors;
		
	static float op[] = new float[3];
	static float op1[] = new float[3];
	static float op_result[] = new float[3];

	static float rho;
	static float theta;
	static float beta=(5/10);

	

	public static void main(String args[])
	{
		int i;
		int t1;
		Integer z1;
		
		try
		{
	
			System.out.println("\t\tDeception Detection in Reputation Management for P2P networks");
	

			
			System.out.print("Enter the number of Peers:");		//Gets the number of peers	
			numPeers=Integer.parseInt(in.readLine());
	
			for(i=0;i<numPeers;i++)
			{
		
				p[i]=new peer();
			}

			
			findneighbor();		//determines neighbors for each peer 
			selectAgent();		//randomly selects the Requesting and Goal Agents
			findwit(ar,ag);		//determines the witnesses for Goal Agent

			System.out.println("\nThe witnesses:"+witlist+"\n");
			if(witlist.size()>0)
			{
				//System.out.println("\nThe witnesses:"+witlist);
				generateRating();	//Generates the trustRatings of the witnesses
			
		
				if(p[ar].neighbor.contains(ag))		//Direct interaction between Ar and Ag
				{
					System.out.println("\nRequesting agent Ar has direct interaction with the Goal Agent Ag");
	
					System.out.println("Final Ratings:");
					System.out.println("m({T})="+p[ar].trust);
					System.out.println("m({~T})="+p[ar].untrust);
					System.out.println("m({T,~T})="+p[ar].uncertainity);
				
					System.exit(0);
				
				}
		
				else if(witlist.size()==1 && !witlist.contains(ar))	//One Witness no need for aggregation
				{
				
					z1=(Integer)(witlist.get(0));
					t1=z1.intValue(); 
					p[ar].weights[t1]=1;
					
					p[ar].wit_trust[t1]=(p[ar].weights[t1]*p[t1].trust); 
					p[ar].wit_untrust[t1]=(p[ar].weights[t1]*p[t1].untrust);
					p[ar].wit_uncertainity[t1]=(p[ar].weights[t1]*p[t1].uncertainity);
	
	
					System.out.println("There is only one witness"+p[t1].trust);
				
					System.out.println("m({T})="+p[ar].wit_trust[t1]);
					System.out.println("m({~T})="+p[ar].wit_untrust[t1]);
					System.out.println("m({T,~T})="+p[ar].wit_uncertainity[t1]);
	
				}
				else		//More than one witness
				{
			
				
					System.out.println("\n\tWitnesses\t\tWeights assigned");
					for(i=0;i<witlist.size();i++)
					{
						z1=(Integer)(witlist.get(i));
						t1=z1.intValue(); 
						p[ar].weights[t1]=1;
					
						p[ar].wit_trust[t1]=(p[ar].weights[t1]*p[t1].trust); 
						p[ar].wit_untrust[t1]=(p[ar].weights[t1]*p[t1].untrust);
						p[ar].wit_uncertainity[t1]=(p[ar].weights[t1]*p[t1].uncertainity);
						
						System.out.println("\t  P"+t1+"\t\t\t  "+p[ar].weights[t1]);
					}
					aggregateTrust();	//aggregates the trust ratings from all witnesses
					
					System.out.println("\nUpdated Weights\n");
					System.out.println("\tWitness\tProbability\tTheta\tNew weight");
			
					for(i=0;i<witlist.size();i++)
					{
						z1=(Integer)(witlist.get(i));
						t1=z1.intValue();
				
						p[ar].probTrust[t1]=(p[t1].trust+p[t1].uncertainity)/(1+p[t1].uncertainity);
						theta=1-((1-beta)*(Math.abs(p[ar].probTrust[t1]-rho)));
					
						BigDecimal b1 = new BigDecimal(p[ar].probTrust[t1]);
						b1 = b1.setScale(2, BigDecimal.ROUND_HALF_UP);
						p[ar].probTrust[t1] = b1.floatValue();

						BigDecimal b2 = new BigDecimal(theta);
						b2 = b2.setScale(2, BigDecimal.ROUND_HALF_UP);
						theta = b2.floatValue();

						p[ar].updateWeight(t1,theta);	//updates the weights of witnesses
					
						System.out.println("\tP"+t1+"\t"+p[ar].probTrust[t1]+"\t\t"+theta+"\t"+p[ar].weights[t1]);
					
					}
					
				}
		
			}
			else
			{
				System.out.println("The Goal Agent is Unknown");
			}
		}
		catch(Exception e)
		{
			System.out.println("Error"+e);
		}
				
	}	

	public static  void findneighbor() throws IOException
	{

		
		int i,j;
		int diff;
		int k,prev=0;
		
		System.out.print("Enter the number of Neighbors:");
		numNeighbors=Integer.parseInt(in.readLine());
	
		for(i=0;i<numPeers;i++)
		{
			randlist[i]=i;
			limit=i;
		}
		
		for(i=0;i<=limit;i++)
		{	
		
			if((p[randlist[i]].neighbor.size())<numNeighbors)
			{
			
				diff=numNeighbors-(p[randlist[i]].neighbor.size());
				for(j=0;j<diff;j++)
				{	
					do
					{
						k=Math.abs(rand.nextInt(limit+1));
					}while(i==k||prev==k);


					if(p[randlist[k]].neighbor.size()<numNeighbors)
					{
					 if((!p[randlist[i]].neighbor.contains(k)) && (!p[randlist[k]].neighbor.contains(i))) 
						{
							
							p[randlist[i]].neighbor.add(k);
			   				p[randlist[k]].neighbor.add(i);
		
						}
						prev=k;
					}
						
				}

			}

		}


	}

	
	public static void selectAgent()
	{
		do
		{
			ar=Math.abs((rand.nextInt()))%numPeers;
	
		}while(p[randlist[ar]].neighbor.size()<=0);


		do
		{
			ag=Math.abs((rand.nextInt()))%numPeers;
	
		}while(ar==ag);
		
		System.out.println("\nRequesting Agent: P"+ar);
		System.out.println("Goal Agent: P"+ag);

				
	}


	public static void findwit(int r,int g)
	{
		int i;
		int[] t=new int[5];
		Integer z;

		if(!visited.contains(r))
		{

			visited.add(r);
			System.out.println("\nVisited node:"+r);
			if(count<=4)
			{
				count++;
				if(p[r].neighbor.contains(g))
				{
					if(!witlist.contains(r))
					{
						witlist.add(r);
						
					}
					return;
				
				}
				else
				{
					for(i=0;i<p[r].neighbor.size();i++)
					{	
						z=(Integer)(p[r].neighbor.get(i));
						t[i]=z.intValue();
					} 
					for(i=0;i<p[r].neighbor.size();i++)
					{	
						
						findwit(t[i],g);
					}
	
				}
				count--;
			}
			else
			{
				System.out.println("\nDepth Limit Reached for"+r);
				return;
			}
		}						

	}
		
	public static void generateRating()
	{
		int i;
		int t3;
		Integer z3;
		int x,y,z;

		for(i=0;i<witlist.size();i++)
		{
			z3=(Integer)(witlist.get(i));
			t3=z3.intValue();

			x=Math.abs(rand.nextInt()%10);
			y=Math.abs(rand.nextInt()%(10-x));
			z=10-(x+y);
			
			p[t3].setRating(x,y,z);
		}

	}

	public static void aggregateTrust()
	{	
		int t2;
		Integer z2;
		int i;
			
		z2=(Integer)(witlist.get(0));
		t2=z2.intValue();

		op[0]=p[ar].wit_trust[t2];
		op[1]=p[ar].wit_untrust[t2];
		op[2]=p[ar].wit_uncertainity[t2];
		
		
				
		for(i=1;i<witlist.size();i++)
		{
			z2=(Integer)(witlist.get(i));
			t2=z2.intValue(); 


			op1[0]=p[ar].wit_trust[t2];
			op1[1]=p[ar].wit_untrust[t2];
			op1[2]=p[ar].wit_uncertainity[t2];
			
			cross(op,op1);
			

			op[0]=op_result[0];
			op[1]=op_result[1];
			op[2]=op_result[2];
		}
		rho=op_result[0];

		System.out.println("\nTrust Ratings After aggregation\n");
		System.out.println("m({T})="+op_result[0]);
		System.out.println("m({~T})="+op_result[1]);
		System.out.println("m({T,~T})="+op_result[2]);
		
	}

	
	public static void cross(float s1[],float s2[])
	{

		int i,j;
		float denom;
		float multab[][] = new float[3][3];
		
		for(i=0;i<3;i++)
		{
			System.out.println("\n");
			for(j=0;j<3;j++)
			{

				multab[i][j]  =Math.abs((float)(s1[i]*s2[j]));
				BigDecimal b4 = new BigDecimal(multab[i][j]);
				b4 = b4.setScale(2, BigDecimal.ROUND_HALF_UP);
				multab[i][j] = b4.floatValue();

			}

		}
			
		denom=(float)(1-(multab[0][1]+multab[1][0]));
		op_result[0]=Math.abs((float)((multab[0][0]+multab[0][2]+multab[2][0])/denom));
		op_result[1]=Math.abs((float)((multab[1][1]+multab[1][2]+multab[2][1])/denom));
		op_result[2]=Math.abs((float)(multab[2][2]/denom));

		for(i=0;i<3;i++)
		{
			BigDecimal b = new BigDecimal(op_result[i]);
			b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
			op_result[i] = b.floatValue();
		}

	}
	
	
}

	