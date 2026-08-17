import os
import sys

# Ensure current directory is in path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from agent import run_agent

def test_followup_pipeline():
    session_id = "test_session_101"
    print("==================================================================")
    print("TEST 1: Initial Request ('I am feeling sad and overwhelmed')")
    print("==================================================================")
    res1 = run_agent(message="I am feeling sad and overwhelmed", session_id=session_id)
    print(f"Result 1 -> Mood: '{res1['mood']}', Genre: '{res1['genre']}', IsFollowup: {res1['is_followup']}")
    print(f"Books count: {len(res1['books'])}")
    for i, b in enumerate(res1['books']):
        print(f"  [{i+1}] {b['title']} (Author: {b['author']}, Year: {b.get('published_year')}, Rating: {b.get('rating')})")

    assert not res1['is_followup'], "Test 1 failed: Initial request should not be a follow-up!"
    assert len(res1['books']) > 0, "Test 1 failed: No books returned!"

    print("\n==================================================================")
    print("TEST 2: Follow-up Request 1 ('which one is the oldest?')")
    print("==================================================================")
    res2 = run_agent(message="which one is the oldest?", session_id=session_id)
    print(f"Result 2 -> Mood: '{res2['mood']}', Genre: '{res2['genre']}', IsFollowup: {res2['is_followup']}")
    print(f"Summary: {res2.get('followup_summary')}")
    print(f"Books count: {len(res2['books'])}")
    for i, b in enumerate(res2['books']):
        print(f"  [{i+1}] {b['title']} (Year: {b.get('published_year')}, Rating: {b.get('rating')})")

    assert res2['is_followup'], "Test 2 failed: Request should be recognized as follow-up!"
    years = [b.get('published_year') for b in res2['books'] if b.get('published_year') is not None]
    initial_years = [b.get('published_year') for b in res1['books'] if b.get('published_year') is not None]
    if initial_years:
        expected_min = min(initial_years)
        assert all(y == expected_min for y in years), f"Test 2 failed: Expected all books to have min year {expected_min}, got {years}"

    print("\n==================================================================")
    print("TEST 3: Follow-up Request 2 ('which is the best rated?')")
    print("==================================================================")
    res3 = run_agent(message="which is the best rated?", session_id=session_id)
    print(f"Result 3 -> Mood: '{res3['mood']}', Genre: '{res3['genre']}', IsFollowup: {res3['is_followup']}")
    print(f"Summary: {res3.get('followup_summary')}")
    print(f"Books count: {len(res3['books'])}")
    for i, b in enumerate(res3['books']):
        print(f"  [{i+1}] {b['title']} (Rating: {b.get('rating')}, Year: {b.get('published_year')})")

    assert res3['is_followup'], "Test 3 failed: Request should be recognized as follow-up!"
    ratings = [float(b.get('rating')) for b in res3['books'] if b.get('rating') is not None]
    initial_ratings = [float(b.get('rating')) for b in res1['books'] if b.get('rating') is not None]
    if initial_ratings:
        expected_max = max(initial_ratings)
        assert all(r == expected_max for r in ratings), f"Test 3 failed: Expected all books to have max rating {expected_max}, got {ratings}"

    print("\n==================================================================")
    print("TEST 4: Follow-up Request 3 ('tell me more about the second one')")
    print("==================================================================")
    res4 = run_agent(message="tell me more about the second one", session_id=session_id)
    print(f"Result 4 -> Mood: '{res4['mood']}', Genre: '{res4['genre']}', IsFollowup: {res4['is_followup']}")
    print(f"Summary: {res4.get('followup_summary')}")
    print(f"Books count: {len(res4['books'])}")
    for i, b in enumerate(res4['books']):
        print(f"  [{i+1}] {b['title']} - Description: {b.get('description')}")

    assert res4['is_followup'], "Test 4 failed: Request should be recognized as follow-up!"
    assert len(res4['books']) == 1, "Test 4 failed: Expected exactly 1 book for specific detail request!"

    print("\n==================================================================")
    print("TEST 5: New Request ('I am feeling happy and want a fun adventure')")
    print("==================================================================")
    res5 = run_agent(message="I am feeling happy and want a fun adventure", session_id=session_id)
    print(f"Result 5 -> Mood: '{res5['mood']}', Genre: '{res5['genre']}', IsFollowup: {res5['is_followup']}")
    print(f"Books count: {len(res5['books'])}")
    for i, b in enumerate(res5['books']):
        print(f"  [{i+1}] {b['title']} (Genre: {b['genre']}, Year: {b.get('published_year')}, Rating: {b.get('rating')})")

    assert not res5['is_followup'], "Test 5 failed: New mood request should not be a follow-up!"

    print("\n==================================================================")
    print("ALL TESTS PASSED SUCCESSFULLY!")
    print("==================================================================")

def main():
    test_followup_pipeline()

if __name__ == "__main__":
    main()
